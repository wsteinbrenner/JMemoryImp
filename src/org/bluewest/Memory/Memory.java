package org.bluewest.Memory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Memory {
	
	//Options
	
	//- Game
	private final static int CARD_SIBLINGS = 2;
	private final static int BOARD_ROWS = 10;
	private final static boolean SHOW_CARDS = !true;
	private final static int GAME_AI_STRENGHT = 10;
	
	//-- Commands
	private final static String CS_QUIT = "q";
	private final static String C_QUIT = "quit";
	private final static String CS_EXIT = "e";
	private final static String C_EXIT = "exit";
	private final static String CS_PRINT = "p";
	private final static String C_PRINT = "print";
	private final static String CS_HELP = "h";
	private final static String C_HELP = "help";
		
	//-- Errors
	private static enum CONTROL_CODE {
		OK,
		EXIT,
		INFO_CARD_MATCH,
		INFO_CARD_NOT_MATCH,
		ERR_VALUE_TOO_SMALL,
		ERR_ROW_NOT_FOUND,
		ERR_COL_NOT_FOUND,
		ERR_CARD_IS_REVEALED
	}
	
	private static enum VIEW_CONTROL_CODE {
		OK,
		QUIT,
		CANCLE,
		PLAY,
		ERR_INPUT_NOT_INT
	}
	
	//- View
	private final static String COL_FILLER = " ";
	private final static String ROW_SIGN = "-";
	private final static String COL_SIGN = "|";
	private final static char SHADOW_SIGN = '*';
	private final static String LINE_END = System.lineSeparator();
	
	//-- Cards 
	private final static int SIGN_FIRST = 65;
	private final static int SIGN_LAST = 90;
	private final static int SIGN_RANGE = SIGN_LAST - SIGN_FIRST;
	
	
	//Models
	
	//- Game
	private int gameCardSiblings = CARD_SIBLINGS;
	private String[][] gameBoard = null;
	private boolean[][] gameShadowBoard = null;
	
	//-- Player
	private String[] playerNames = new String[0];
	private int[] playerPoints = new int[0];
	private int[] playerGuessRows = new int[0];
	private int[] playerGuessCols = new int[0];
	
	//-- Board
	private int boardRows = BOARD_ROWS;
	private int boardMaxRowElements = 0;
	private int boardMaxElements = 0;
	
	//- View
	private final Scanner input = new Scanner(System.in);
	private int boardViewRowWith = 0;
	private int boardViewCardValueWith = 0;
	private int boardViewColWith = 0;
	private String boardViewCardShadowValue = "";
	

	public static void main(String[] args) {
		final Memory game = new Memory();
		game.viewGameInit();
		game.viewGamePlay();
	}	
	
	// Modell
	
	public CONTROL_CODE setBoardRows(int amount) {
		
		if(amount < 1) {
			return CONTROL_CODE.ERR_VALUE_TOO_SMALL;
		}
		
		boardRows = amount;
		
		return CONTROL_CODE.OK;
	}
	
	public CONTROL_CODE setGameCardSiblings(int amount) {

		if(amount < CARD_SIBLINGS) {
			return CONTROL_CODE.ERR_VALUE_TOO_SMALL;
		}
		
		this.gameCardSiblings = amount;
		
		return CONTROL_CODE.OK;
	}
	
	private void modelInitPlayers(String[] playerNames) {
		this.playerNames = playerNames;
		playerPoints = new int[playerNames.length];
	}	
	
	private boolean modelGetShadowBoard(int guessIndex) {			
		int row = playerGuessRows[guessIndex];
		int col = playerGuessCols[guessIndex];
		return gameShadowBoard[row][col];
		
	}

	private void modelSetShadowBoard(int guessIndex, boolean mask) {
		
		int row = playerGuessRows[guessIndex];
		int col = playerGuessCols[guessIndex];
		gameShadowBoard[row][col] = mask;
	}

	private CONTROL_CODE modelSetShadowBoard(int row, int col, boolean mask) {
		
		CONTROL_CODE codeCheck = engineIsInBoardRange(row, col);
		
		if( CONTROL_CODE.OK != codeCheck) {
			return codeCheck;
		}
		
		gameShadowBoard[row][col] = mask;
		
		return CONTROL_CODE.OK;
	}

	private CONTROL_CODE modelResetShadowBoard(int[] row, int[] col) {
		
		for(int i = 0; i < row.length; i++) {
			
			final CONTROL_CODE code = modelSetShadowBoard(row[i], col[i], !SHOW_CARDS);
			
			if(code != CONTROL_CODE.OK) {
				return code;
			}
		}
		
		return CONTROL_CODE.OK;
	}

	private String modelGetCardSign(int[] roundIndex) {
		
		String cardSign = "";
		
		for(int i = roundIndex.length-1; i >= 0; i--) {
			
			if(roundIndex[i] < 0) {
				continue;
			}
			
			cardSign += (char) (roundIndex[i] + SIGN_FIRST);
		}
		
		return cardSign;
	}

	private String modelGetGuess(int guessNumber) {
		return gameBoard[playerGuessRows[guessNumber]][playerGuessCols[guessNumber]];
	}

	private CONTROL_CODE setCol(String value, final int row, final int col) {
		
		final CONTROL_CODE check = engineIsInBoardRange(row, col);
		
		if(CONTROL_CODE.OK.equals(check)) {
			gameBoard[row][col] = value;
		}			
		
		return check;
	}

	private String getCol(final int row, final int col) {
		return gameBoard[row][col];
	}

	public int[] getPlayerHighPoint() {
		
		int[] playersHighPoint = new int[playerPoints.length];
		Arrays.fill(playersHighPoint, -1);
		
		for(int i = 0, pointIndex = 0, playerHighPoint = 0; i < playerPoints.length; i++) {
			
			if(playerPoints[i] > playerHighPoint) {
				playerHighPoint = playerPoints[i];
				playersHighPoint = new int[playerPoints.length];
				Arrays.fill(playersHighPoint, -1);
				pointIndex = 0;
				playersHighPoint[pointIndex] = i;
				pointIndex++;
			}
			
			if(playerPoints[i] == playerHighPoint) {
				playersHighPoint[pointIndex] = i;
				pointIndex++;
			}			
			
		}
		
		return playersHighPoint;
	}

	private int getGameCardsRevealed() {
		
		int sum = 0;
		
		for(int i = 0; i < playerPoints.length; i++) {
			sum += playerPoints[i];
		}
		
		return sum * gameCardSiblings;		
	}
			
	
	//View

	public void viewGameInit() {
		drawMainWelcome();
		drawMainConfigure();
		drawMainAskConfiguration();		
		drawMainGameIntro();
		drawMainAskPlayers();
	}
	
	private void viewInitBoardView() {
		viewInitBoardViewCardShadowValue(this.boardViewCardValueWith);
		viewInitBoardViewRowWith(boardMaxRowElements, boardViewColWith);
	}

	private void viewInitBoardViewRowWith(int rowElements, int colWith) {
		this.boardViewRowWith = (boardMaxRowElements * ROW_SIGN.length() * (boardViewColWith + COL_SIGN.length())) + COL_SIGN.length();
	}

	private void viewInitBoardViewCardShadowValue(int colWith) {
		for(int i = 0; i< colWith; i++) {
			this.boardViewCardShadowValue += String.valueOf(SHADOW_SIGN);
		}
	}

	private void viewInitBoardCardValueWith( final String[][] board) {
		
		int maxColWith = 0;
		
		for (int row = 0; row < board.length; row++) {
	
			final String[] col = board[row];		
	
			for (int i = 0; i < col.length; i++) {
				
				int colWith = board[row][i].length();
				
				if(colWith > maxColWith) {
					maxColWith = colWith;
				}
			}
		}
		
		this.boardViewCardValueWith = maxColWith;		
	}

	private void viewInitBoardColWith(int cardValueWith) {
		this.boardViewColWith = cardValueWith + (COL_FILLER.length() *2);
	}

	public void viewGamePlay() {
				
		modelInitPlayers(this.playerNames);
		engineInitGame(this.boardRows);
		
		int player = 0;
		
		do {
						
			drawGameStats();
			drawPlayerInCharge(player);
			final VIEW_CONTROL_CODE command = drawAskGameCommand();
			
			if(drawGameAskQuit(command == VIEW_CONTROL_CODE.QUIT)) {
				break;
			}
			
			player = drawPlayersTurn(player);
			
			if(enginGetGameCardsLeft() <= 0) {
				break;
			}
			
		} while (player < playerNames.length);
		
		drawGameFinished();		
	}
	
	private boolean viewHandleEngineError(CONTROL_CODE code) {
		
		if(CONTROL_CODE.OK == code) {
			return false;
		}
		
		drawMainControlMessage(code);
		
		return true;
	}

	private boolean viewIsExit(String input) {
		
		if (C_EXIT.equalsIgnoreCase(input) || CS_EXIT.equalsIgnoreCase(input)) {
			return true;
		}
		
		return false;
	}

	private boolean viewIsYes(String input) {
		return "y".equalsIgnoreCase(input);
	}

	private void draw(String text) {
		System.out.print(text);
	}

	private String drawReadString(String text) {
		drawNotEmpty(text);
		return input.next();		
	}
	
	private int drawReadInt(String text) {
		drawNotEmpty(text);
		
		while(!input.hasNextInt()) {
			draw("Invalid Input: " + input.next());draw(LINE_END);
			draw("Please type a number: ");
		}
		
		return input.nextInt();
	}

	private void drawNotEmpty(String text) {
		if (text != null && !text.isEmpty()) {
			draw(text);
		}
	}

	private void drawMainGameIntro() {
		draw(LINE_END);
		draw(LINE_END);
		draw("Lets play !!");
		draw(LINE_END);
	}

	private void drawMainAskPlayers() {
		
		draw("Type player names, (a).i. for Computer and (e)xit when finished:");
		draw(LINE_END);
		draw(LINE_END);
		
		int playerNumber = 0;
		boolean initPlayer = true;
		
		while(initPlayer) {
			
			String input = drawReadString("Player " + playerNumber + ": ");
			
			if(drawGameAskYesNo(viewIsExit(input), "Do you really want to exit? (y/n)")){
				initPlayer = false;
				continue;
			}
			
			if("a".equals(input)) {
				
				if(VIEW_CONTROL_CODE.CANCLE == drawMainAskAiPlayer(playerNumber)) {
					continue;
				}
				
				input = "A.I-" + playerNumber;
			}			
			
			playerNames = engineUtilArrayAddElement(playerNames, input);
			
			playerNumber ++;
		}
	}
	
	private VIEW_CONTROL_CODE drawMainAskAiPlayer(int player) {
		
		int strenght = -1;
		boolean ask = true;
		
		while(ask) {
			strenght = drawReadInt("Please give a.i. strenght. ( 0 - " + GAME_AI_STRENGHT +", the higher the stronger)");
			
			if(strenght < 0 || strenght > GAME_AI_STRENGHT) {
				
				draw("Ivalid input. Minimum: 0, Maximum: 10 ");
				draw(LINE_END);
				
				if(drawGameAskYesNo(true, "Do you want to exit? (y/n)")){
					return VIEW_CONTROL_CODE.CANCLE;
				}
				
				continue;
			}
			
			ask = false;
		}
		
		addAiPlayer(player, strenght);
		
		return VIEW_CONTROL_CODE.OK;
		
	}

	private void drawGameStats() {
		drawBoardLine();
		draw(LINE_END);
		drawPlayers();
		drawBoardLine();
		draw(LINE_END);
		drawBoard(this.gameBoard, this.gameShadowBoard);
		draw(LINE_END);
		draw(LINE_END);
	}

	private void drawGameHint(String hint) {
		
		drawNotEmpty(hint);
		
		try {
			System.in.read();
		} catch (IOException e) {
			draw("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}

	private boolean drawGameAskQuit(boolean isQuit) {		
		return drawGameAskYesNo(isQuit, "Do you really want to quit? (y/n) ");		
	}

	private boolean drawGameAskYesNo(boolean isCommand, String question) {
		
		if(!isCommand) {
			return false;
		}
		
		return viewIsYes(drawReadString(question));		
	}

	private void drawBoardHeader() {		
		for(int i = 0; i < this.boardMaxRowElements; i++) {
			drawBoardCol(String.valueOf(i), true);
		}
		
		draw(COL_SIGN);
	}

	private void drawBoardLine() {
		for(int i = 0; i < this.boardViewRowWith; i++) {
			draw(ROW_SIGN);
		}
	}

	private void drawGameBoard() {
		drawBoard(gameBoard, gameShadowBoard);
		draw(LINE_END);
	}
	
	private void drawBoard(final String[][] board, final boolean[][] shadowBoard) {
		drawBoardHeader();
		draw(LINE_END);
		drawBoardRows(board, shadowBoard);
	}
	
	private void drawBoardRow(final String[] cols, final boolean[] shadowCols) {
		drawBoardLine();
		draw(LINE_END);
		drawBoardCols(cols, shadowCols);
		return;
	}

	private void drawBoardRows(final String[][] board, final boolean[][] shadowBoard) {
		
		int rowIndex = 0;
		
		for(int i = 0; i < board.length; i++) {
			drawBoardRow(board[i], shadowBoard[i]);
			drawBoardCol(String.valueOf(rowIndex), true);
			draw(LINE_END);
			rowIndex++;
		}
		
		drawBoardLine();
	}

	private void drawBoardCol(String colValue, boolean unmask) {
		draw(COL_SIGN);
		draw(COL_FILLER);
		drawCardValue(unmask ? colValue : boardViewCardShadowValue);
		draw(COL_FILLER);
	}

	private void drawBoardCols(String[] cols, boolean[] shadowCols) {
		
		for(int i = 0; i < cols.length; i++) {
			drawBoardCol(cols[i], shadowCols[i]);
		}
		
		return;
	}

	private void drawCardValue(String colValue) {
		
		String cardValue = "";
		
		for(int i = 0; i < this.boardViewCardValueWith; i++) {
			
			if( this.boardViewCardValueWith <= i + colValue.length()) {
				break;
			}
			
			cardValue += " ";
		}
		
		draw(cardValue);
		draw(colValue);
	}
	
	private void drawCards(String[] cards) {		
		for(String card : cards) {
			draw(card);
			draw(COL_SIGN);
		}
	}
	
	private void drawPlayers() {
		
		for(int i = 0; i < playerNames.length; i++) {
			drawPlayerIndex(i);
			drawPlayer(i);
			draw(LINE_END);			
		}
		
	}
	
	private void drawPlayer(int playerNumber) {
		draw(playerNames[playerNumber] + ": ");
		draw(String.valueOf(playerPoints[playerNumber]));
	}
	
	private void drawPlayerIndex(int index) {
		draw("[Player " + index + "] ");
	}
	
	private void drawPlayerInCharge(int player) {
		drawPlayerIndex(player);
		draw(playerNames[player] + "'s guess: ");
		draw(LINE_END);	
	}

	private void drawMainWelcome() {
		draw(LINE_END);
		draw("--------------------"); draw(LINE_END);
		draw("A simple Memory Game"); draw(LINE_END);
		draw("--------------------"); draw(LINE_END);
		draw(LINE_END);
	}

	private void drawMainConfigure() {
		draw(LINE_END);
		draw("Configure Game"); draw(LINE_END);
		draw("--------------"); draw(LINE_END);
		draw(LINE_END);
		drawMainConfigureHelp();
		
	}

	private void drawMainConfigureHelp() {
		draw("* Configure the amount of (r)ows the board shall have"); draw(LINE_END);
		draw("* Configure the amount of (c)ard siblings"); draw(LINE_END);
		draw("* (h)elp"); draw(LINE_END);
		draw("* (p)rint"); draw(LINE_END);
		draw("* (e)xit"); draw(LINE_END);
	}

	private void drawMainControlMessage(CONTROL_CODE code) {
		drawMainControlMessage(code, "");
	}

	private void drawMainControlMessage(CONTROL_CODE code, String text) {
		
		switch (code) {
		case OK:
			draw("O.K. "); draw(LINE_END);
			break;
		case EXIT:
			draw("User requestet exit. "); draw(LINE_END);
			break;
		case ERR_VALUE_TOO_SMALL:
			draw("Value too small. ");drawNotEmpty(text); draw(LINE_END);
			break;
		case ERR_COL_NOT_FOUND:
			draw("Column not in range. ");drawNotEmpty(text); draw(LINE_END);
			break;
		case ERR_ROW_NOT_FOUND:
			draw("Row not in range. ");drawNotEmpty(text); draw(LINE_END);
			break;
		case ERR_CARD_IS_REVEALED:
			draw("The card has allready been revealed. ");drawNotEmpty(text); draw(LINE_END);
			break;
	
		default:
			draw("Err: Control Code not found: " + code); draw(LINE_END);
		}
		
		
	}

	private void drawMainAskConfiguration() {
		
		draw("Configure Game"); draw(LINE_END);
				
		boolean configure = true;
		CONTROL_CODE code = CONTROL_CODE.OK;
		String message = "";
		
		while(configure) {
			
			final String input = drawReadString("Configure (r)ow, (card) or (h)elp, (p)rint, (e)xit: ");
	
			switch (input) {
			case "r":
			case "rows":
				code = setBoardRows(drawReadInt("Amount of rows (Default: " + BOARD_ROWS + "): "));
				message = "Row value has to be greater 0";
				break;
			case "c":
			case "card":
				code = setGameCardSiblings(drawReadInt("Amount of card siblings (Default: " + CARD_SIBLINGS + "): "));
				message = "Sibling value has to be greater than " + (CARD_SIBLINGS - 1) ;
				break;
			case CS_EXIT:
			case C_EXIT:
				code = CONTROL_CODE.EXIT;
				message = "Exit configuration.";
				configure = false; 
				break;
			case CS_PRINT:
			case C_PRINT:
				drawMainPrintConfiguration();
				code = CONTROL_CODE.OK;
				break;
			case CS_HELP:
			case C_HELP:
				drawMainConfigureHelp();
				code = CONTROL_CODE.OK;
				break;
			default:
				draw("Unknown Command: " + input);draw(LINE_END);
				break;
			}
			
			if(CONTROL_CODE.OK != code) {
				drawMainControlMessage(code, message);
			}
			
			message = "";			
		}
		
		
	}

	private void drawMainPrintConfiguration(){
		draw("Amount Board Rows:" + boardRows); draw(LINE_END);
		draw("Amount Matchin Siblings:" + gameCardSiblings); draw(LINE_END);
	}

	private VIEW_CONTROL_CODE drawAskGameCommand() {
		
		
		boolean ask = true;
	
		do {
			final String input = drawReadString("What do you want to do? (p)lay, (q)uit ");
	
			switch (input) {
			case C_QUIT:
			case CS_QUIT:
				ask = false;
				return VIEW_CONTROL_CODE.QUIT;
			case "p":
			case "play":
				ask = false;
				return VIEW_CONTROL_CODE.PLAY;
			default:
				draw("Unknown Command: " + input);
				draw(LINE_END);
				break;
			}
	
		} while (ask);
		
		return VIEW_CONTROL_CODE.OK;
	}

	private void drawAskPlayersGuess(int guess) {
		
		CONTROL_CODE code = CONTROL_CODE.OK;
		
		do {
			int row = 0;
	
			do {
				row = drawReadInt("Row[" + guess + "]: ");
				code = engineSetPlayerGuessRow(guess, row);
				viewHandleEngineError(code);
			} while (CONTROL_CODE.OK != code);
	
			int col = 0;
	
			do {
				col = drawReadInt("Col[" + guess + "]: ");
				code = engineSetPlayerGuessCol(guess, row, col);
				viewHandleEngineError(code);
			} while (CONTROL_CODE.OK != code);
	
		} while (viewHandleEngineError(engineDoRevealCard(guess, SHOW_CARDS)));
		
	}

	private CONTROL_CODE drawMoveResult(CONTROL_CODE code) {
		
		if(CONTROL_CODE.INFO_CARD_MATCH == code) {
			draw("Congratulation, you found a match!");
			return CONTROL_CODE.OK;
		} 
	
		if (CONTROL_CODE.INFO_CARD_NOT_MATCH == code) {
			drawGameHint("Sorry, no match  (Enter to Continue)");
			return CONTROL_CODE.OK;
		} 
		
		viewHandleEngineError(code);
		
		return code;
	}

	private void drawGameFinished() {
		
		int[] playersWon = getPlayerHighPoint();
		
		draw("Congratulation!!");
		draw(LINE_END);
		
		for(int playerWon : playersWon) {
			draw(LINE_END);
			draw(playerNames[playerWon]);
			draw(" has won");
		}
		
		draw(LINE_END);
		draw(LINE_END);
		draw("Thank you for playing. I hope I'll see you soon! Bye-Bye");draw(LINE_END);
		
	}

	private int drawPlayersTurn(int player) {
		
		drawAskPlayerGuess();
		
		if(drawMoveResult(engineDoMove(player)) != CONTROL_CODE.OK){
			return player;
		}
		
		if(player + 1 >= playerNames.length) {
			return 0;
		}
		
		return player + 1;
	}

	private void drawAskPlayerGuess() {
		
		playerGuessRows = new int[gameCardSiblings];
		playerGuessCols = new int[gameCardSiblings];
		String lastCard = null;
		
		for(int guessIndex = 0; guessIndex < gameCardSiblings; guessIndex++) {
			
			drawAskPlayersGuess(guessIndex);				
			drawGameBoard();
			
			final String card = modelGetGuess(guessIndex);
			
			if(!engineDoCardsMatch(lastCard, card)) {
				return;
			}
			
			lastCard = card;			
		}
	}	
	
	// Controller
	
	private void engineInitBoardMaxRowElements( final String[][] board) {
				
		int maxRowElements = 0;
		
		for (int row = 0; row < board.length; row++) {

			final int rowElements = board[row].length;
			
			if(maxRowElements < rowElements) {
				maxRowElements = rowElements;
			}			
		}
		
		this.boardMaxRowElements = maxRowElements;
	}
	
	private void engineInitBoardMaxElements(String[][] board) {
		
		int boardElements = 0;
		
		for(String[] row : board) {
			boardElements += row.length;
		}
		
		this.boardMaxElements = boardElements;
	}
	
	private void engineInitBoardDimensions( final String[][] board) {
		viewInitBoardCardValueWith(board);
		engineInitBoardMaxRowElements(board);
		viewInitBoardColWith(this.boardViewCardValueWith);		
	}
	
	private CONTROL_CODE engineIsInBoardRange(final int row, final int col) {
		
		if(row >= gameBoard.length) {
			return CONTROL_CODE.ERR_ROW_NOT_FOUND;
		}
		
		if(col >= gameBoard[row].length) {
			return CONTROL_CODE.ERR_COL_NOT_FOUND;
		}	
		
		return CONTROL_CODE.OK;
	}
	
	
	private void engineInitBoard(int boardWith) {
		engineInitBoards(boardWith);		
		engineInitBoardDimensions(this.gameBoard);
		viewInitBoardView();
	}

	private void engineInitGame(int boardWith) {
		engineInitBoard(boardWith);
		
	}

	private void engineInitBoards(int width){		
		
		this.gameBoard = new String[width][width * gameCardSiblings];
		this.gameShadowBoard = new boolean[width][width * gameCardSiblings];
		
		engineInitBoardMaxElements(gameBoard);
		
		final String[] cards = engineCardDeckShuffle(engineGetCardDeck(gameBoard));  
		
		for(int row = 0, card = 0; row < gameBoard.length; row ++) {
			
			for(int col = 0; col < gameBoard[row].length; col++, card++) {
				gameBoard[row][col] = cards[card];
				gameShadowBoard[row][col] = !SHOW_CARDS;
			}
		}
	}

	public CONTROL_CODE engineDoMove(int player) {
		
		modelResetShadowBoard(playerGuessRows, playerGuessCols);
		
		CONTROL_CODE check = engineCheckGuess();
		if(check == CONTROL_CODE.INFO_CARD_NOT_MATCH) {
			return check;
		}
		
		check = engineDoMove();
		if(check != CONTROL_CODE.OK) {
			return check;
		}
		
		playerPoints[player]++;
		
		return CONTROL_CODE.INFO_CARD_MATCH;
	}

	protected CONTROL_CODE engineDoMove() {
	
		for (int guess = 0; guess < CARD_SIBLINGS; guess++) {
			
			final CONTROL_CODE code = engineDoRevealCard(guess, SHOW_CARDS);
	
			if (code != CONTROL_CODE.OK) {
				return code;
			}
		}
		
		return CONTROL_CODE.OK;
	}

	protected CONTROL_CODE engineCheckGuess() {
	
		String lastCard = null;
	
		for (int guess = 0; guess < CARD_SIBLINGS; guess++) {
	
			int row = playerGuessRows[guess];
			int col = playerGuessCols[guess];
			final String card = getCol(row, col);
	
			if (!engineDoCardsMatch(lastCard, card)) {
				return CONTROL_CODE.INFO_CARD_NOT_MATCH;
			}
	
			lastCard = card;
		}
		
		return CONTROL_CODE.INFO_CARD_MATCH;
		
	}

	protected CONTROL_CODE engineDoRevealCard(int guessIndex, boolean mask) {
		
		if(mask == modelGetShadowBoard(guessIndex)) {
			return CONTROL_CODE.ERR_CARD_IS_REVEALED;
		}
		
		modelSetShadowBoard(guessIndex, mask);
		
		return CONTROL_CODE.OK;		
	}
	
	public CONTROL_CODE engineSetPlayerGuessRow(int guess, int row) {
		if(row >= boardRows || row < 0) {
			return CONTROL_CODE.ERR_ROW_NOT_FOUND;
		}
		
		playerGuessRows[guess] = row;
		
		return CONTROL_CODE.OK;
	}

	public CONTROL_CODE engineSetPlayerGuessCol(int guess, int row, int col) {
		
		if(row >= boardRows || row < 0) {
			return CONTROL_CODE.ERR_ROW_NOT_FOUND;
		}
		
		if(col >= gameBoard[row].length || col < 0) {
			return CONTROL_CODE.ERR_COL_NOT_FOUND;
		}
		
		playerGuessCols[guess] = col;
		
		return CONTROL_CODE.OK;
	}

	private boolean engineDoCardsMatch(String first, String second) {		
		return first == null || first.equals(second);
	}
		

	private int enginGetGameCardsLeft() {
		return boardMaxElements - getGameCardsRevealed();
	}

	private String[] engineGetCardDeck(String[][] board) {	
		final String[] cards = new String[boardMaxElements];
		final int[] roundIndex = new int[(int) (cards.length / SIGN_RANGE)];
		Arrays.fill(roundIndex, -1);
		
		for (int i = 0; i < cards.length;) {
			engineCardDeckIncrement(roundIndex);
			final String cardSign = modelGetCardSign(roundIndex);
			
			for(int duplicate = 0; duplicate < gameCardSiblings; duplicate++) {
				cards[i++] = cardSign;
			}
		}
		
		return cards;
	}
	
	private void engineCardDeckIncrement(int[] roundIndex) {
						
		for(int i = 0; i < roundIndex.length; i++) {
			
			if(roundIndex[i] < SIGN_RANGE) {
				roundIndex[i]++;
				break;
			}
			
			roundIndex[i] = 0;
		}		
	}

	private String[] engineCardDeckShuffle(String[] cards) {
		
		final String[] shuffledCards = new String[cards.length];
		final Random randGen = new Random(System.nanoTime());		
		
		for(int i = 0; i < shuffledCards.length; i++) {
			
			final String[] reducedCards = new String[cards.length -1];
			int index = randGen.nextInt(cards.length);
			
			for(int cardI = 0, reducedCardI = 0; cardI < cards.length; cardI++, reducedCardI++) {
				
				if(cardI == index) {
					shuffledCards[i] = cards[cardI];
					reducedCardI--;
					continue;
				}
				
				reducedCards[reducedCardI] = cards[cardI];
				
			}			
			
			cards = reducedCards;			
		}
		
		return shuffledCards;
	}
	
	protected int engineUtilRandGetInt(int border) {
		return engineRandGen.nextInt(border);
	}
	
	protected String[] engineUtilArrayAddElement(String[] values, String value) {
		final String[] result = Arrays.copyOf(values, values.length+1);
		result[values.length] = value;
		return result;
	}

	protected int[] engineUtilArrayAddElement(int[] values, int value) {
		final int[] result = Arrays.copyOf(values, values.length+1);
		result[values.length] = value;
		return result;
	}

	protected int[][] engineUtilArrayAddElement(int[][] container, int index) {
		
		if(container.length  <= index) {
			container = Arrays.copyOf(container, index + 1);
		}
		
		if(container[index] == null) {
			container[index] = new int[0];
		}
		
		return container;
	}

	protected String[][] engineUtilArrayAddElement(String[][] container, int index) {
		
		if(container.length  <= index) {
			container = Arrays.copyOf(container, index + 1);
			container[index] = new String[0];
		}
		
		if(container[index] == null) {
			container[index] = new String[0];
		}
		
		return container;
	}

	protected String[] engineUtilArrayRemoveByIndex(String[] values, int[] indices) {
		
		if(indices.length <= 0) {
			return values;
		}
		
		final String[] result = new String[values.length - indices.length];
		
		for (int i = 0, match = 0; i < values.length && match < indices.length; i++) {
	
			if (engineUtilArrayContains(indices, i)) {
				match++;
				i++;
				continue;
			}
	
			result[i - match] = values[i];
		}
		
		return result;
	}

	protected int[] engineUtilArrayRemoveByIndex(int[] values, int[] indices) {
		
		if(indices.length <= 0) {
			return values;
		}
		
		final int[] result = new int[values.length - indices.length];
		
		for (int i = 0, match = 0; i < values.length && match < indices.length; i++) {
	
			if (engineUtilArrayContains(indices, i)) {
				match++;
				i++;
				continue;
			}
	
			result[i - match] = values[i];
		}
		
		return result;
	}

	protected boolean engineUtilArrayContains(int[] values, int value) {
		
		for(int i = 0; i < values.length; i++) {
			
			if(values[i] == value) {
				return true;
			}
		}
		
		return false;
	}

	protected int[] engineUtilArraysGetValueIndeces(int[] values, int value) {
		
		int[] valueIndices = new int[0];
		
		for(int i = 0; i < values.length; i++) {
			
			if( value != values[i]) {
				continue;
			}
			
			valueIndices = engineUtilArrayAddElement(valueIndices, i);			
		}
		
		return valueIndices;
	}

	protected int[] engineUtilArraysGetValueIndeces(String[] values, String value) {
		
		int[] valueIndices = new int[0];
		
		for(int i = 0; i < values.length; i++) {
			
			if(!value.equals(values[i])) {
				continue;
			}
			
			valueIndices = engineUtilArrayAddElement(valueIndices, i);			
		}
		
		return valueIndices;
	}

	private final static Random engineRandGen = new Random(System.nanoTime());
	private int[] aiPlayerStrenght = new int[0];
	private String[][] aiCardValues = new String[0][];
	private int[][] aiCardRows = new int[0][];
	private int[][] aiCardCols = new int[0][];
	
	
	protected boolean isAiPlayer(int player) {
		return (player < aiPlayerStrenght.length) 
				&& (getAiPlayerStrenght(player) > 0); 
	}
	
	protected int getAiPlayerStrenght(int player) {
		return aiPlayerStrenght[player] - 1;
	}
	
	public void addAiPlayer(int player, int strenght) {
		addAiPlayerStrenght(player, strenght);
	}
	
	private void addAiPlayerStrenght(int player, int strenght) {		
		
		if(aiPlayerStrenght.length  <= player) {
			aiPlayerStrenght = Arrays.copyOf(aiPlayerStrenght, player + 1);
		}
		
		aiPlayerStrenght[player] = strenght + 1;
	}
	
	protected void addAiMoves(int row, int col) {
		
		for(int player = 0; player < aiPlayerStrenght.length; player++) {
			
			if(!isAiPlayer(player)) {
				continue;
			}
			
			addAiMove(player, row, col);
		}
	}
	
	private void addAiMove(int player, int row, int col) {
		
		if(!aiDoMove(player)) {
			return;
		}
		
		final String cardValue = getCol(row, col);
		
		
		if(hasAiMove(player, cardValue, row, col)) {
			return;
		}
		
		addAiCardValue(player, cardValue);
		addAiCardRow(player, row);
		addAiCardCol(player, col);
	}
		
	protected void removeAiMoves(int row, int col) {
		
		for(int player = 0; player < aiPlayerStrenght.length; player++) {
			
			if(!isAiPlayer(player)) {
				continue;
			}
			
			removeAiMove(player, row, col);
		}
	}
	
	private void removeAiMove(int player, int row, int col) {
		final String cardValue = getCol(row, col);
		final int[] valueIndeces = getAiCardValues(player, cardValue);		
		removeAiCardValue(player, valueIndeces);
		removeAiCardRow(player, valueIndeces);
		removeAiCardCol(player, valueIndeces);
	}
	
	private boolean hasAiMove(int player, String cardValue, int row, int col) {

		final int[] valueIndeces = getAiCardValues(player, cardValue);

		for (int index = 0; index < valueIndeces.length; index++) {
			final int aiIndex = valueIndeces[index];
			final int aiRow = getAiCardRow(player, aiIndex);
			final int aiCol = getAiCardCol(player, aiIndex);

			if (aiRow == row && aiCol == col) {
				return true;
			}
		}

		return false;
	}
	
	private void addAiCardValue(int player, String cardValue) {
		aiCardValues = engineUtilArrayAddElement(aiCardValues, player);
		aiCardValues[player] = engineUtilArrayAddElement(aiCardValues[player], cardValue);
	}
	
	private void addAiCardRow(int player, int row) {
		aiCardRows = engineUtilArrayAddElement(aiCardRows, player);
		aiCardRows[player] = engineUtilArrayAddElement(aiCardRows[player], row);		
	}
	
	private int getAiCardRow(int player, int index) {
		return aiCardRows[player][index];
	}
	
	private void addAiCardCol(int player, int col) {
		aiCardCols = engineUtilArrayAddElement(aiCardCols, player);
		aiCardCols[player] = engineUtilArrayAddElement(aiCardCols[player], col);		
	}
	
	private int getAiCardCol(int player, int index) {
		return aiCardCols[player][index];
	}
	
	private int[] getAiCardValues(int player, String value) {
		return engineUtilArraysGetValueIndeces(aiCardValues[player], value);
	}
	
	private void removeAiCardValue(int player,  int[] values) {
		aiCardValues[player] = engineUtilArrayRemoveByIndex(aiCardValues[player], values);		
	}
	
	private void removeAiCardRow(int player, int[] values) {
		aiCardRows[player] = engineUtilArrayRemoveByIndex(aiCardRows[player], values);		
	}
	
	private void removeAiCardCol(int player, int[] values) {
		aiCardCols[player] = engineUtilArrayRemoveByIndex(aiCardCols[player], values);		
	}
	
	private boolean aiDoMove(int player) {
		
		final int chance = engineUtilRandGetInt(GAME_AI_STRENGHT);
		final int aiStrenght = getAiPlayerStrenght(player);
		
		return !(chance > aiStrenght);
	}
	
	private int[][] aiGetMove(int player){		
		//TODO: implement logic
		return null;
		
	}
	
	

}
