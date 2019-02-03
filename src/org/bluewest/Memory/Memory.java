package org.bluewest.Memory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Memory {
	
	//Options
	
	//- Game
	private final static int CARD_SIBLINGS = 2;
	private final static boolean SHOW_CARDS = true;
	
	//-- Commands
	private final static String CS_QUIT = "q";
	private final static String C_QUIT = "quit";
	private final static String CS_EXIT = "e";
	private final static String C_EXIT = "exit";
		
	//-- Errors
	private static enum ERR_CODE {
		OK,
		ERR_ROW_NOT_FOUND,
		ERR_COL_NOT_FOUND
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
	private String[] playerNames = new String[2];
	private int[] playerPoints = new int[2];
	
	//-- Board
	private int boardColWith = 0;
	private int boardViewRowWith = 0;
	private int boardMaxRowElements = 0;
	private int boardMaxElements = 0;
	
	//- View
	private final Scanner input = new Scanner(System.in);
	private int boardCardValueWith = 0;
	private String boardViewCardShadowValue = "";
	

	public static void main(String[] args) {
		final Memory game = new Memory();
		game.gamePlay(10);
	}
	
	// Static View
	
	private static void drawStatic(String text) {
		System.out.print(text);
	}
	
	
	
	
	private void drawMainWelcome() {
		drawStatic(LINE_END);
		drawStatic("--------------------"); drawStatic(LINE_END);
		drawStatic("A simple Memory Game"); drawStatic(LINE_END);
		drawStatic("--------------------"); drawStatic(LINE_END);
		drawStatic(LINE_END);
	}

	// Controller

	public void gamePlay(int boardWith) {
		
		drawMainWelcome();
		
		boolean run = true;
		
//		while(run) {
//			
//		}
		
		drawGameIntro();		
		initGame(boardWith);
		
		for (int player = 0; player < playerNames.length; player++) {			
			
			drawGameStats();
			drawPlayerInCharge(player);					
			
			final int[] row = new int[gameCardSiblings];
			final int[] col = new int[gameCardSiblings];
			String card = null;
			boolean match = false;
			
			for(int cardIndex = 0; cardIndex < gameCardSiblings; cardIndex++) {
				
				String input = drawReadString("Row[" + cardIndex + "]: ");
				
				if(drawGameAskQuit(isQuit(input))) {
					run = false;
					return;
				}
				
				row[cardIndex] = Integer.valueOf(input);
				
				input = drawReadString("Col[" + cardIndex + "]: ");

				if(drawGameAskQuit(isQuit(input))) {
					run = false;
					return;
				}
				
				col[cardIndex] = Integer.valueOf(input);
				
				setShadowBoard(row[cardIndex], col[cardIndex], SHOW_CARDS);
				
				drawBoard(this.gameBoard, this.gameShadowBoard);
				
				draw(LINE_END);
				
				final String value = getCol(row[cardIndex], col[cardIndex]);
				
				if(stopCompare(card, value)) {
					match = false;
					break;
				}
				
				card = value;
				match = true;				
			}
			
			if(match) {
				playerPoints[player]++;
				draw("Congratulation, you found a match: " + card);
				match = false;
			}else {
				resetShadowBoard(row, col);
				drawGameHint("Sorry, no match  (Enter to Continue)");
			}
			
			if(player + 1 >= playerNames.length) {
				player = 0;
			}
			
			if(gameCardsLeft() > 0) {
				continue;
			}
			
			int[] playersWon = getPlayerHighPoint();
			draw("Congratulation!!");
			draw(LINE_END);
			
			for(int playerWon : playersWon) {
				draw(LINE_END);
				draw(playerNames[playerWon]);
				draw(" has won");
			}
			
			draw(LINE_END);
			
			if(drawGameAskYesNo(true, "Do you want to play again? (y/n)")) {
				continue;
			}
			
			run = false;
		}
	}
	
	private void initGame(int boardWith) {
		initPlayer();
		initBoard(boardWith);
		
	}
	
	private void initPlayer() {
		
		draw("Set player names, (e)xit when finished:");
		draw(LINE_END);
		draw(LINE_END);
		
		int playerNumber = 0;
		boolean initPlayer = true;
		
		while(initPlayer) {
			
			final String input = drawReadString("Player " + playerNumber + ": ");
			
			if(drawGameAskYesNo(isExit(input), "Do you really want to exit? (y/n)")){
				initPlayer = false;
				continue;
			}
			
			if(playerNumber >= playerNames.length) {
				playerNames = Arrays.copyOf(playerNames, playerNames.length+1);
				playerPoints = Arrays.copyOf(playerPoints, playerPoints.length+1);
			}			
			
			playerNames[playerNumber] = input;
			playerPoints[playerNumber] = 0;
			playerNumber ++;
		}
	}
	
	//Model
	
	private void intBoards(int width){		
		
		this.gameBoard = new String[width][width * gameCardSiblings];
		this.gameShadowBoard = new boolean[width][width * gameCardSiblings];
		
		initBoardMaxElements(gameBoard);
		
		final String[] cards = shuffleCards(getCards(gameBoard));  
		
		for(int row = 0, card = 0; row < gameBoard.length; row ++) {
			
			for(int col = 0; col < gameBoard[row].length; col++, card++) {
				gameBoard[row][col] = cards[card];
				gameShadowBoard[row][col] = !SHOW_CARDS;
			}
		}
	}	
	
	//View
	
	private void initBoardViewRowWith(int rowElements, int colWith) {
		this.boardViewRowWith = (boardMaxRowElements * ROW_SIGN.length() * (boardColWith + COL_SIGN.length())) + COL_SIGN.length();
	}

	private void initBoardViewCardShadowValue(int colWith) {
		for(int i = 0; i< colWith; i++) {
			this.boardViewCardShadowValue += String.valueOf(SHADOW_SIGN);
		}
	}

	private void draw(String text) {
		System.out.print(text);
	}
	
	private String drawReadString(String text) {
		drawNotEmpty(text);
		return input.next();		
	}

	private void drawNotEmpty(String text) {
		if (text != null && !text.isEmpty()) {
			draw(text);
		}
	}

	private void drawGameIntro() {
		draw(LINE_END);
		draw(LINE_END);
		draw("Lets play !!");
		draw(LINE_END);
	}

	private void drawGameStats() {
		drawBoardLine();
		draw(LINE_END);
		drawPlayers();
		drawBoardLine();
		draw(LINE_END);
		drawBoard(this.gameBoard, this.gameShadowBoard);
		draw(LINE_END);
		draw("Type (q)uit to quit game");
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
		return drawGameAskYesNo(isQuit, "Do you really want to quit? (y/n)");		
	}

	private boolean drawGameAskYesNo(boolean isCommand, String question) {
		
		if(!isCommand) {
			return false;
		}
		
		return isYes(drawReadString(question));		
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
		
		//for(String[] cols : board) {
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

	//View
		
	private boolean isQuit(String input) {
		
		if (C_QUIT.equalsIgnoreCase(input) || CS_QUIT.equalsIgnoreCase(input)) {
			return true;
		}
		
		return false;
	}

	private boolean isExit(String input) {
		
		if (C_EXIT.equalsIgnoreCase(input) || CS_EXIT.equalsIgnoreCase(input)) {
			return true;
		}
		
		return false;
	}

	private boolean isYes(String input) {
		return "y".equalsIgnoreCase(input);
	}
	
	// Controller

	private void gameInitBoardCardValueWith( final String[][] board) {
		
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
	
	private void gameInitBoardColWith(int cardValueWith) {
		this.boardColWith = cardValueWith + (COL_FILLER.length() *2);
	}
	
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
		gameInitBoardCardValueWith(board);
		initBoardMaxRowElements(board);
		gameInitBoardColWith(this.boardCardValueWith);		
	}
	
	private void initBoardView() {
		initBoardViewCardShadowValue(this.boardCardValueWith);
		initBoardViewRowWith(boardMaxRowElements, boardColWith);
	}
	
	private void initBoard(int boardWith) {
		intBoards(boardWith);		
		initBoardDimensions(this.gameBoard);
		initBoardView();
	}
	
	//Controller
	
	private ERR_CODE isInBoardRange(final int row, final int col) {
		
		if(gameBoard.length >= row) {
			return ERR_CODE.ERR_ROW_NOT_FOUND;
		}
		
		if(gameBoard[row].length >= col) {
			return ERR_CODE.ERR_COL_NOT_FOUND;
		}	
		
		return ERR_CODE.OK;
	}
	
	private void setShadowBoard(int row, int col, boolean mask) {
		gameShadowBoard[row][col] = mask;
	}

	private void resetShadowBoard(int[] row, int[] col) {
		
		for(int i = 0; i < row.length; i++) {
			setShadowBoard(row[i], col[i], !SHOW_CARDS);
		}
	}

	private int getGameCardsRevealed() {
		
		int sum = 0;
		
		for(int i = 0; i < playerPoints.length; i++) {
			sum += playerPoints[i];
		}
		
		return sum * gameCardSiblings;		
	}

	private boolean stopCompare(String first, String second) {
		
		if(first == null || first.equals(second)) {
			return false;
		}
		
		return true;
	}

	private int gameCardsLeft() {
		return boardMaxElements - getGameCardsRevealed();
	}

	private String[] getCards(String[][] board) {	
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
	
	private ERR_CODE setCol(String value, final int row, final int col) {
		
		final ERR_CODE check = isInBoardRange(row, col);
		
		if(ERR_CODE.OK.equals(check)) {
			gameBoard[row][col] = value;
		}			
		
		return check;
	}
	
	private String getCol(final int row, final int col) {
		return gameBoard[row][col];
	}
	
	private int[] getPlayerHighPoint() {
		
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
	
	
	
	

}
