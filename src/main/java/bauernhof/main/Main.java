package bauernhof.main;

import bauernhof.gameboard.GameBoard;
import bauernhof.gameconfig.GameConfiguration;
import bauernhof.gameconfig.GameConfigurationParser;
import bauernhof.gui.FarmPanel;
import bauernhof.networking.C2SFarmConnection;
import bauernhof.player.HumanPlayer;
import bauernhof.player.RandomAIPlayer;
import bauernhof.player.ScoreDiscrepancyException;
import bauernhof.player.UninitializedPlayerException;
import bauernhof.preset.*;
import java.awt.Color;
import bauernhof.preset.networking.C2SConnection;
import bauernhof.preset.networking.RemoteException;
import bauernhof.preset.networking.S2CConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import bauernhof.preset.card.Card;
import sag.SAGFrame;


/**
 * Main class to bring everything together and execute the game.
 * @author Jonas Härtner
 */
public class Main {

    /**
     * Main method to be run when project JAR file is executed.
     * @param args List of (optional) arguments handled by the {@link ArgumentParser}.
     */
    public static void main(String[] args) {

        //Lists for the ArgumentParser.
        ArrayList<String> projectauthors = new ArrayList<>();
        projectauthors.add("Nils Wüstefeld");
        projectauthors.add("Jonas Härtner");
        projectauthors.add("Maxim Strzebkowski");
        projectauthors.add("Corinna Liley");
        String projectname = "Grow Man's Sky";
        ArrayList<OptionalFeature> optionalFeatures= new ArrayList<>();
        optionalFeatures.add(OptionalFeature.SOUNDEFFECTS);
        optionalFeatures.add(OptionalFeature.SCREENSHOTS);
        ArgumentParser argumentParser = new ArgumentParser(args, projectname, "Version 1", projectauthors, optionalFeatures);

        //Different flags handled by the ArgumentParser to be used for by the server, client, or non-network game.
        List<String> playerNames = argumentParser.playerNames;
        List<PlayerType> playerTypes = argumentParser.playerTypes;
        List<Color> playerColors = argumentParser.playerColors;
        long delay = argumentParser.delay;
        File configFile = argumentParser.gameConfigurationFile;
        boolean showGUI = argumentParser.showGUI;
        int volume = argumentParser.volume;

        /*
        As described in the JavaDoc of bauernhof.preset.Settings, there is no extra flag for determining whether an instance
        of the game is meant to run as a server or a client for network gaming. Rather, a server connection is established
        if there is a REMOTE player among the player types and the game is meant to run in client mode if the -con flag
        is set, i.e. a network address is assigned to connectToHostname.
         */
        boolean server = playerTypes.contains(PlayerType.REMOTE);
        boolean client = (argumentParser.connectToHostname != null);

        System.out.println("Game Initialized with players:\n" + playerNames);
        System.out.println("Of type:\n" + playerTypes);

        if (client) {
            /*
            The client has its own GameConfiguration via the GameConfigurationParser and the raw configuration data transmitted by
            the server. Hence, all it has to do is establish a C2SConnection and use the values specified by the ArgumentParser.
             */
            Socket socket = null;
            C2SConnection connection = null;
            try {
                socket = new Socket(argumentParser.connectToHostname, argumentParser.port);
            } catch (Exception e) {
                System.out.println("Could not connect to server socket");
                e.printStackTrace();
            }
            try {
                connection = new C2SFarmConnection(socket, new GameConfigurationParser(), projectname, playerTypes.get(0),
                        delay, playerColors, showGUI, volume, playerNames.get(0));
            } catch (IOException e) {
                System.out.println("Could not establish client to server connection");
                e.printStackTrace();
            }
            /*
            handlePackets() blocks the connection and lets it keep receiving data from the server. Once the connection
            is established and open, it can start receiving.
            */
            if (connection.isOpen()) {
                try {
                    connection.handlePackets();
                } catch (RemoteException | IOException e) {
                    System.out.println("Error handling server packets: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        }
        else {
            /*
            Both the server and the local game need to establish a starting point for the game by receiving a set of cards
            from the GameConfiguration and shuffling it (as a list). The GameBoard, however, does not need the initial draw
            pile of all cards at the start, but the draw pile after all players have drawn their cards. Therefore, since the
            number of cards drawn by all players if each of them draws their starting hand is numplayers * numCardsPerPlayerHand,
            everything from the topmost card to that point will be removed before constructing the main GameBoard.
             */
            GameConfiguration config = getConfig(new GameConfigurationParser(), configFile);

            int numCardsPerPlayerHand = config.getNumCardsPerPlayerHand();
            int numDepositionAreaSlots = config.getNumDepositionAreaSlots();
            int numplayers = playerTypes.size();

            Set<Card> cards = config.getCards();
            List<Card> baseDrawPile = new ArrayList<>(cards);
            Collections.shuffle(baseDrawPile);
            ImmutableList<Card> initialDrawPile = new ImmutableList<>(baseDrawPile);
            List<Card> playerHandsDrawn = new ArrayList<>(initialDrawPile);
            playerHandsDrawn.subList(0, (numplayers * numCardsPerPlayerHand)).clear();
            GameBoard board = new GameBoard(numplayers, playerHandsDrawn);
            List<Player> players = new ArrayList<>(numplayers);

            SAGFrame frame = new SAGFrame("Grow Man's Sky", 30, 1280, 720);
            FarmPanel panel = new FarmPanel(1600, 900, board, frame, volume);

            Socket socket = null;
            S2CConnection serverCon;

            if (server) {
                //Opening a server connection. The programming will be blocked at this point until a client connects.
                socket = makeServerAvailable(argumentParser.port);
            }

            //Constructing the appropriate players and adding them to the list.
            for (int i = 0; i < numplayers; i++) {
                switch (playerTypes.get(i)) {
                    case HUMAN:
                        Player humanPlayer = new HumanPlayer(playerNames.get(i), panel);
                        players.add(i, humanPlayer);
                        showGUI = true;
                        break;
                    case RANDOM_AI:
                        Player randomPlayer = new RandomAIPlayer(playerNames.get(i), delay);
                        players.add(i, randomPlayer);
                        break;
                    case REMOTE:
                        try {
                            serverCon = new S2CConnection(socket);
                            serverCon.setPlayerNames(new ImmutableList<>(playerNames));
                            players.add(i, serverCon.getRemotePlayer());
                        } catch (NullPointerException | IOException e) {
                            System.out.println("Could not establish server connection to remote player");
                            e.printStackTrace();
                        }
                        break;
                }
            }

            /*
            Since the players are stored in a list of players, but the IDs are meant to be numbers between 1 and 4 instead of
            0 and 3, we need the +1 for the ID. Everyone gets initialized and the player hands are set on the main GameBoard
            similar to how the player hands are set on the individual player boards when init(...) is called on an instance of
            BasicPlayer.
             */
            for (Player p : players) {
                try {
                    int playerid = players.indexOf(p) + 1;
                    p.init(config, initialDrawPile, numplayers, playerid);

                    int offset = (playerid - 1) * numCardsPerPlayerHand;
                    List<Card> initialPlayerHand = new ArrayList<>();

                    for (int i = offset; i < offset + numCardsPerPlayerHand; i++) {
                        initialPlayerHand.add(initialDrawPile.get(i));
                    }

                    board.setPlayerCards(playerid, initialPlayerHand);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Player initialized twice");
                }
            }

            //Updating the player scores to show the scores right even for the initial hands, before any card was played.
            board.updateAllPlayerScores();

            //showGUI is automatically true if there is a human player. Can also be optionally given as an argument.
            if (showGUI){
                try {
                    panel.initPanel(players, playerColors);
                } catch (Exception e) {
                    System.out.println("Could not initialize panel");
                    e.printStackTrace();
                }
                frame.setSAGPanel(panel);
                    frame.setVisible(true);
            }

            /*
            Iterating through the round of the game. The game either goes on for 30 rounds or until there are enough
            cards on the discard pile. The number of cards is given in the GameConfiguration. Each turn of each round the
            turn count gets increased, the gui gets updated (if it is visible), and the current player is requested to make a move.
            After that, the move is made on the main GameBoard and every other player gets updated.
             */
            Move currentMove = null;
            rounds:
            for (int i = 0; i < 30; i++) {
                for (Player p : players) {
                    if (board.getDiscardPile().getSize() == numDepositionAreaSlots) {
                        break rounds;
                    }

                    GameBoard.increaseTurnCount();

                    if (showGUI) {
                        try {
                            panel.updateGUI(p);
                        } catch (Exception e) {
                            System.out.println("Could not update panel");
                        }
                    }
                    try {
                        currentMove = p.request();
                        System.out.println(currentMove);
                        board.makeMove(currentMove);
                        System.out.println("Scores: " + board.getPlayerScoresAsList());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (Player oP : players) {
                        if (oP.equals(p)) {
                            continue;
                        }
                        try {
                            oP.update(currentMove);
                        } catch (Exception e) {
                            System.out.println("Could not update other player: " + (players.indexOf(oP) + 1));
                            e.printStackTrace();
                        }
                    }

                    /*
                    This is purely for display purposes and has no functional necessity. It just looks nicer when there
                    is a little pause at the end of each turn. The value was experimentally found to work well.
                    */
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println("Sleep at the end of turn interrupted");
                        e.printStackTrace();
                    }
                }
            }

            /*
            Calculating the winner in order to show the winner graphic at the end.
             */
            ImmutableList<Integer> finalScores = new ImmutableList<>(board.getPlayerScoresAsList());

            Integer maxScore = Collections.max(finalScores);
            int argmax = finalScores.indexOf(maxScore);
            Player winningPlayer = players.get(argmax);
            String winnerName = null;
            try {
                winnerName = winningPlayer.getName();
            } catch (Exception e) {
                System.out.println("Could not get winner name");
                e.printStackTrace();
            }

            if (showGUI) {
                try {
                    panel.updateGUI(players.get(board.calculateCurrentPlayerId() - 1));
                } catch (Exception e) {
                    System.out.println("Could not update GUI");
                    e.printStackTrace();
                }
            }

            /*
            Score verification. If the client does not throw a RemoteException, it automatically disconnects and the server
            catches another type of exception (presumably SocketException). This is caught and ignored in our case since
            it indicated that the verify method was successful.
             */
            for (Player p : players) {
                try {
                    p.verifyGame(finalScores);
                }
                catch (RemoteException | UninitializedPlayerException | ScoreDiscrepancyException e) {
                    System.out.println("Could not verify game");
                    e.printStackTrace();
                    return;
                }
                catch (Exception e) {
                    System.out.println("Client possibly dissconnected, verification supposedly successful");
                }
            }

            //Showing the winner and the winner graphic if there's a gui
            System.out.println("Winner: " + winnerName + " Score: " + maxScore);

            if (showGUI) {
                try {
                    panel.showWinner(winnerName, maxScore);
                } catch (Exception e) {
                    System.out.println("Cannot show winner");
                    e.printStackTrace();
                }
            }



        }

    }

    /**
     * A helper method to establish an open socket on the server side.
     * @param port The number of the port the client can connect to.
     * @return The {@link Socket} for the server to use in the {@link S2CConnection}
     */
    private static Socket makeServerAvailable(int port) {
        Socket socket = null;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port: " + port);
            socket = serverSocket.accept();
            System.out.println("New client connected on port: " + port);

        } catch (IOException e) {
            System.out.println("Problem with server: " + e.getMessage());
            e.printStackTrace();
        }

        return socket;
    }

    /**
     * A helper method to generate a {@link GameConfiguration} from an XML configuration file and a {@link GameConfigurationParser}.
     * @param configParser The {@link GameConfigurationParser} to be used here.
     * @param configFile The XML game configuration file.
     * @return The {@link GameConfiguration}.
     */
    private static GameConfiguration getConfig(GameConfigurationParser configParser, File configFile) {
        GameConfiguration config = null;

        try {
            if (configFile == null) {
                config = configParser.parse(new File("src/main/ressources/bauernhof.xml"));
            } else {
                config = configParser.parse(configFile);
            }
        } catch (GameConfigurationException | IOException e) {
            System.out.println("Could not obtain config from ConfigParser");
            e.printStackTrace();
        }

        return config;
    }
}

//}
