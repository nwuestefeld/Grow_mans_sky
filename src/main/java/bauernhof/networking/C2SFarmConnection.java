package bauernhof.networking;

import bauernhof.gameboard.GameBoard;
import bauernhof.gui.FarmPanel;
import bauernhof.player.BasicPlayer;
import bauernhof.player.HumanPlayer;
import bauernhof.player.RandomAIPlayer;
import bauernhof.preset.*;
import bauernhof.gameconfig.GameConfigurationParser;
import bauernhof.preset.card.Card;
import bauernhof.preset.networking.RemoteException;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import bauernhof.preset.networking.C2SConnection;
import sag.SAGFrame;
import bauernhof.preset.PlayerType;

/**
 * Class to establish a connection between a client and a server.
 * @author Jonas Härtner
 */
public class C2SFarmConnection extends C2SConnection {

    /**
     * The {@link PlayerType} of the player on the client.
     */
    private final PlayerType playerType;

    /**
     * The local {@link Player} on the client.
     */
    private Player player;

    /**
     * A list of Colors to display the players.
     */
    private final List<Color> playerColors;

    /**
     * Possible delay to be used for a {@link RandomAIPlayer}.
     */
    private final long delay;

    /**
     * Boolean to determine whether to show the GUI or not (automatically true if human player)
     */
    private final boolean showGUI;

    /**
     * Connection used to throw {@link RemoteException}.
     */
    private final Socket connection;

    /**
     * The panel to display the GUI on the client screen.
     */
    private FarmPanel panel;

    /**
     * The main {@link GameBoard} handling the game logic.
     */
    private GameBoard board;

    /**
     * List of players for the GUI to display them.
     */
    private List<Player> players;

    /**
     * The volume should sounds want to be played. Default is 0.
     */
    private final int volume;

    /**
     * The name of the local player as it appears on the client's screen. Might be different from the server.
     */
    private final String playerName;

    /**
     * Constructor for {@link C2SFarmConnection}.
     * @param connection The {@link Socket} through which the connection is established.
     * @param gameConfigurationParser The {@link GameConfigurationParser} to receive the config transmitted as a raw String.
     * @param projectName The name of the project.
     * @param playerType The type of player the client wishes to use.
     * @param delay The delay in case the client player is a {@link RandomAIPlayer}.
     * @param playerColors The colors the GUI should use for the players.
     * @param showGUI Whether to show the GUI or not. Defaults to true if a human player is playing.
     * @param volume The volume of sounds.
     * @param playerName The name of the player as it should appear on the client side.
     * @throws IOException Might be thrown by the super class.
     */
    public C2SFarmConnection(Socket connection, GameConfigurationParser gameConfigurationParser, String projectName,
                             PlayerType playerType, long delay, List<Color> playerColors, boolean showGUI, int volume,
                             String playerName) throws IOException {
        super(connection, gameConfigurationParser, projectName);
        this.connection = connection;
        this.playerType = playerType;
        this.delay = delay;
        this.playerColors = playerColors;
        this.volume = volume;
        this.playerName = playerName;

        if (playerType == PlayerType.HUMAN) {
            this.showGUI = true;
        } else {
            this.showGUI = showGUI;
        }
    }

    /**
     * Initializes the client. Gets called by the server connection when the {@link bauernhof.preset.networking.RemotePlayer}
     * is initialized.
     * @param config The game configuration received from the server.
     * @param initialDrawPile The initial draw pile from which all players draw their hands at the start.
     * @param playerNames The list of names used to display the other players as they are shown on the server.
     * @param playerid The player ID of the client player.
     * @throws UnsupportedOperationException If the wrong {@link PlayerType} is given, e.g. REMOTE.
     */
    @Override
    protected void onInit(GameConfiguration config, ImmutableList<Card> initialDrawPile, ImmutableList<String> playerNames, int playerid) throws UnsupportedOperationException {
        /*
        Initialize the main GameBoard as well as the FarmPanel similar to how bauernhof.main.Main#main() does it.
         */
        int numCardsPerPlayerHand = config.getNumCardsPerPlayerHand();
        int numplayers = playerNames.size();
        System.out.println("Numplayers: " + numplayers);
        List<Card> playerHandsDrawn = new ArrayList<>(initialDrawPile);
        playerHandsDrawn.subList(0, (numplayers * numCardsPerPlayerHand)).clear();

        board = new GameBoard(numplayers, playerHandsDrawn);
        players = new ArrayList<>(numplayers);
        SAGFrame frame = new SAGFrame("Grow Man's Sky", 30, 1280, 720);
        panel = new FarmPanel(1600, 900, board, frame, volume);

        //Every player besides the local player is established as a BasicPlayer as there is no need to call request() on them.
        for (int i = 0; i < playerNames.size(); i++) {
            if (i != (playerid - 1)) {
                players.add(new BasicPlayer(playerNames.get(i)));
            } else {
                switch (playerType) {
                    case HUMAN:
                        player = new HumanPlayer(playerName, panel);
                        players.add(player);
                        break;
                    case RANDOM_AI:
                        player = new RandomAIPlayer(playerName, delay);
                        players.add(player);
                        break;
                    default:
                        throw new UnsupportedOperationException("Clients can only use PlayerTypes HUMAN or RANDOM_AI");
                }
            }

            //Draw the player's hand for each player, similar to how BasicPlayer does it in init(...).
            int offset = i * numCardsPerPlayerHand;
            List<Card> initialPlayerHand = new ArrayList<>();

            for (int j = offset; j < offset + numCardsPerPlayerHand; j++) {
                initialPlayerHand.add(initialDrawPile.get(j));
            }
            board.setPlayerCards(i + 1, initialPlayerHand);
        }

        //Initializing each player.
        for (Player p : players) {
            try {
                p.init(config, initialDrawPile, numplayers, players.indexOf(p) + 1);
            } catch (Exception e) {
                System.out.println("Player initialization problem");
                e.printStackTrace();
            }
        }

        //Updating to show the scores even for initial hands.
        board.updateAllPlayerScores();

        //Since player 1 starts and the GameBoard starts its TurnCount at 0.
        GameBoard.increaseTurnCount();

        //Make GUI visible if GUI is supposed to be used.
        if (showGUI) {
            try {
                panel.initPanel(players, playerColors);
            } catch (Exception e) {
                System.out.println("Could not initialize panel");
                e.printStackTrace();
            }

            frame.setSAGPanel(panel);
            frame.setVisible(true);

            guiUpdate();
        }
    }

    /**
     * Requests a move from the client player.
     * @return The move which the client player makes.
     * @throws Exception Catches possible exceptions from player request and turns them into {@link RemoteException} for the server to see.
     */
    @Override
    protected Move onRequest() throws Exception {
        try {
            guiUpdate();
            Move myMove = player.request();

            //Prints the move in case of non-GUI game
            System.out.println(myMove);
            board.makeMove(myMove);
            System.out.println("Scores: " + board.getPlayerScoresAsList());

            //Increase turn count after each request.
            GameBoard.increaseTurnCount();
            guiUpdate();
            return myMove;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e.toString(), connection.getLocalSocketAddress());
        }

    }

    /**
     * Used to update client game when a move was made on the server.
     * @param move The move that was made elsewhere.
     * @throws Exception Turns exception from update into {@link RemoteException}.
     */
    @Override
    protected void onUpdate(Move move) throws Exception {
        guiUpdate();

        //Make received move on main board and print it for non-GUI game.
        board.makeMove(move);
        System.out.println(move);
        System.out.println("Scores: " + board.getPlayerScoresAsList());
        try {
            player.update(move);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e.toString(), connection.getLocalSocketAddress());
        }
        GameBoard.increaseTurnCount();

        guiUpdate();
    }

    /**
     * Used to send the local player's score to the server.
     * @return The score of the client player.
     * @throws Exception Turns exceptions thrown by {@link BasicPlayer#getScore()} into {@link RemoteException} and throws it.
     */
    @Override
    protected int onGetScore() throws Exception {
        try {
            System.out.println("Playerscore: " + player.getScore());
            return player.getScore();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e.toString(), connection.getLocalSocketAddress());
        }
    }

    /**
     * Getter for the name of the local player via the server connection.
     * @return The name of the client player.
     * @throws Exception Sends exception to the server indicating that the name is null.
     */
    @Override
    protected String onGetName() throws Exception {
        if (playerName == null) {
            throw new RemoteException("Player name not applicable", "NullPointerException", connection.getLocalSocketAddress());
        }
        else {
            return playerName;
        }
    }

    /**
     * Used by the client player to verify the game on their own {@link GameBoard}.
     * @param scores A list of scores as transmitted by the server.
     * @throws Exception Turns an exception thrown by {@link BasicPlayer#verifyGame(ImmutableList)} into {@link RemoteException}.
     */
    @Override
    protected void onVerifyGame(ImmutableList<Integer> scores) throws RemoteException {
        try {
            player.verifyGame(scores);
            System.out.println("Could successfully verify game");
            Integer maxScore = Collections.max(scores);
            int argmax = scores.indexOf(maxScore);
            Player winningPlayer = players.get(argmax);
            try {
                System.out.println("Winner: " + winningPlayer.getName() + " Score: " + maxScore);
            } catch (Exception e) {
                System.out.println("Could not get winner name");
                e.printStackTrace();
            }
            if (showGUI) {
                guiUpdate();
                panel.showWinner(winningPlayer.getName(), maxScore);
            }
            Thread.sleep(1000);
            disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e.toString(), connection.getLocalSocketAddress());
        }
    }

    /**
     * A helper method to update the GUI should it be shown.
     */
    private void guiUpdate() {
        try {
            if (showGUI) {
                panel.updateGUI(players.get(board.calculateCurrentPlayerId() - 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
