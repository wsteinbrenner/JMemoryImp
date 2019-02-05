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
	private final static boolean SHOW_CARDS = true;
	
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
		PLAY,
		ERR_INPUT_NOT_INT
	}
	
	//- View
	private final static String COL_FILLER = " ";
	private final static String ROW_SIGN = "-";
	private final static String COL_SIGN = "|";
	private final static char SHADOW_SIGN = '*';
	private final static String LINE_END = System.lineSeparator();
	private CONTROL_CODE viewEngineControlCode = CONTROL_CODE.OK;
	
	//-- Cards 
	private final static int SIGN_FIRST = 65;
	private final static int SIGN_LAST = 90;
	private final static int SIGN_RANGE = SIGN_LAST - SIGN_FIRST;
	
	
	//Models
	
	//- Game
	private int gameCardSiblings = CARD_SIBLINGS;
	private String[][] gameBoard = null;
	private boolean[][] gameShadowBoard = null;
	private CONTROL_CODE gameControlCode = CONTROL_CODE.OK;
	
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
	private int boardCardValueWith = 0;
	private int boardColWith = 0;
	private String boardViewCardShadowValue = "";
	private String boardLastSelectedCard = null;
	private boolean boardLastSelectedMatched = false;
	

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
	
	private boolean getShadowBoard(int guessIndex) {			
		int row = playerGuessRows[guessIndex];
		int col = playerGuessCols[guessIndex];
		return gameShadowBoard[row][col];
		
	}

	private void setShadowBoard(int guessIndex, boolean mask) {
		
		int row = playerGuessRows[guessIndex];
		int col = playerGuessCols[guessIndex];
		gameShadowBoard[row][col] = mask;
	}

	private CONTROL_CODE setShadowBoard(int row, int col, boolean mask) {
		
		CONTROL_CODE codeCheck = isInBoardRange(row, col);
		
		if( CONTROL_CODE.OK != codeCheck) {
			return codeCheck;
		}
		
		gameShadowBoard[row][col] = mask;
		
		return CONTROL_CODE.OK;
	}

	private CONTROL_CODE resetShadowBoard(int[] row, int[] col) {
		
		for(int i = 0; i < row.length; i++) {
			
			final CONTROL_CODE code = setShadowBoard(row[i], col[i], !SHOW_CARDS);
			
			if(code != CONTROL_CODE.OK) {
				return code;
			}
		}
		
		return CONTROL_CODE.OK;
	}

	private String getCardSign(int[] roundIndex) {
		
		String cardSign = "";
		
		for(int i = roundIndex.length-1; i >= 0; i--) {
			
			if(roundIndex[i] < 0) {
				continue;
			}
			
			cardSign += (char) (roundIndex[i] + SIGN_FIRST);
		}
		
		return cardSign;
	}

	private String getGuess(int guessNumber) {
		return gameBoard[playerGuessRows[guessNumber]][playerGuessCols[guessNumber]];
	}

	private CONTROL_CODE setCol(String value, final int row, final int col) {
		
		final CONTROL_CODE check = isInBoardRange(row, col);
		
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
		viewInitBoardViewCardShadowValue(this.boardCardValueWith);
		viewInitBoardViewRowWith(boardMaxRowElements, boardColWith);
	}

	private void viewInitBoardViewRowWith(int rowElements, int colWith) {
		this.boardViewRowWith = (boardMaxRowElements * ROW_SIGN.length() * (boardColWith + COL_SIGN.length())) + COL_SIGN.length();
	}

	private void viewInitBoardViewCardShadowValue(int colWith) {
		for(int i = 0; i< colWith; i++) {
			this.boardViewCardShadowValue += String.valueOf(SHADOW_SIGN);
		}
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
			
			if(getGameCardsLeft() <= 0) {
				break;
			}
			
		} while (player < playerNames.length);
		
		drawGameFinished();		
	}
	
	private boolean viewHandleEngineError(CONTROL_CODE code) {
		viewEngineControlCode = code;
		
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
		
		draw("Set player names, (e)xit when finished:");
		draw(LINE_END);
		draw(LINE_END);
		
		int playerNumber = 0;
		boolean initPlayer = true;
		
		while(initPlayer) {
			
			final String input = drawReadString("Player " + playerNumber + ": ");
			
			if(drawGameAskYesNo(viewIsExit(input), "Do you really want to exit? (y/n)")){
				initPlayer = false;
				continue;
			}
			
			if(playerNumber >= playerNames.length) {
				playerNames = Arrays.copyOf(playerNames, playerNames.length + 1);
			}
			playerNames[playerNumber] = input;
			playerNumber ++;
		}
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
		
		for(int i = 0; i < this.boardCardValueWith; i++) {
			
			if( this.boardCardValueWith <= i + colValue.length()) {
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
		boardLastSelectedCard = null;
		boardLastSelectedMatched = false;
		
		for(int guessIndex = 0; guessIndex < gameCardSiblings; guessIndex++) {
			
			drawAskPlayersGuess(guessIndex);				
			drawGameBoard();
			
			final String card = getGuess(guessIndex);
			
			if(!engineDoCardsMatch(boardLastSelectedCard, card)) {
				return;
			}
			
			boardLastSelectedCard = card;			
		}
		
		boardLastSelectedMatched = true;
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
		
		this.boardCardValueWith = maxColWith;		
	}
	
	private void viewInitBoardColWith(int cardValueWith) {
		this.boardColWith = cardValueWith + (COL_FILLER.length() *2);
	}

	
	
	// Controller
	
	private void initBoardMaxRowElements( final String[][] board) {
				
		int maxRowElements = 0;
		
		for (int row = 0; row < board.length; row++) {

			final int rowElements = board[row].length;
			
			if(maxRowElements < rowElements) {
				maxRowElements = rowElements;
			}			
		}
		
		this.boardMaxRowElements = maxRowElements;
	}
	
	private void initBoardMaxElements(String[][] board) {
		
		int boardElements = 0;
		
		for(String[] row : board) {
			boardElements += row.length;
		}
		
		this.boardMaxElements = boardElements;
	}
	
	private void initBoardDimensions( final String[][] board) {
		viewInitBoardCardValueWith(board);
		initBoardMaxRowElements(board);
		viewInitBoardColWith(this.boardCardValueWith);		
	}
	
	private CONTROL_CODE isInBoardRange(final int row, final int col) {
		
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
		initBoardDimensions(this.gameBoard);
		viewInitBoardView();
	}

	private void engineInitGame(int boardWith) {
		engineInitBoard(boardWith);
		
	}

	private void engineInitBoards(int width){		
		
		this.gameBoard = new String[width][width * gameCardSiblings];
		this.gameShadowBoard = new boolean[width][width * gameCardSiblings];
		
		initBoardMaxElements(gameBoard);
		
		final String[] cards = shuffleCards(getGameCardDeck(gameBoard));  
		
		for(int row = 0, card = 0; row < gameBoard.length; row ++) {
			
			for(int col = 0; col < gameBoard[row].length; col++, card++) {
				gameBoard[row][col] = cards[card];
				gameShadowBoard[row][col] = !SHOW_CARDS;
			}
		}
	}

	public CONTROL_CODE engineDoMove(int player) {
		
		resetShadowBoard(playerGuessRows, playerGuessCols);
		
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
		
		if(mask == getShadowBoard(guessIndex)) {
			return CONTROL_CODE.ERR_CARD_IS_REVEALED;
		}
		
		setShadowBoard(guessIndex, mask);
		
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
		
		if(col >= gameBoard[row].length || col < 0) {
			return CONTROL_CODE.ERR_COL_NOT_FOUND;
		}
		
		playerGuessCols[guess] = col;
		
		return CONTROL_CODE.OK;
	}

	private boolean engineDoCardsMatch(String first, String second) {		
		return first == null || first.equals(second);
	}
		

	private int getGameCardsLeft() {
		return boardMaxElements - getGameCardsRevealed();
	}

	private String[] getGameCardDeck(String[][] board) {	
		final String[] cards = new String[boardMaxElements];
		final int[] roundIndex = new int[(int) (cards.length / SIGN_RANGE)];
		Arrays.fill(roundIndex, -1);
		
		for (int i = 0; i < cards.length; i++) {
			incrementCardRoundIndex(roundIndex);
			final String cardSign = getCardSign(roundIndex);
			cards[i] = cardSign;
			cards[++i] = cardSign; 
		}
		
		return cards;
	}
	
	private String[] shuffleCards(String[] cards) {
		
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
	
	private void incrementCardRoundIndex(int[] roundIndex) {
						
		for(int i = 0; i < roundIndex.length; i++) {
			
			if(roundIndex[i] < SIGN_RANGE) {
				roundIndex[i]++;
				break;
			}
			
			roundIndex[i] = 0;
		}		
	}

}
