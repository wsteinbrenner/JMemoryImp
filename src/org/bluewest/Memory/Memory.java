package org.bluewest.Memory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Memory {
	
	private final static int CARD_SIBLINGS = 2;
	//private final static int HEIGHT_FACTOR = 2; // replaced by cardMatchAmount
	private final static String COL_FILLER = " ";
	private final static String ROW_SIGN = "-";
	private final static String COL_SIGN = "|";
	private final static char SHADOW_SIGN = '*';
	private final static String LINE_END = System.lineSeparator();
	private final static int SIGN_FIRST = 65;
	private final static int SIGN_LAST = 90;
	private final static int SIGN_RANGE = SIGN_LAST - SIGN_FIRST;
	private final static boolean SHOW_CARDS = true;
	
	//Commands
	private final static String CS_QUIT = "q";
	private final static String C_QUIT = "quit";
	private final static String CS_EXIT = "e";
	private final static String C_EXIT = "exit";
	
	//Errors
	private static enum ERR_CODE {
		OK,
		ERR_ROW_NOT_FOUND,
		ERR_COL_NOT_FOUND
	}
	
	//Game
	private int cardMatchAmount = 2;
	
	//Board
	private int boardColWith = 0;
	private int boardCardWith = 0;
	private int boardRowWith = 0;
	private int maxRowElements = 0;
	private int boardElements = 0;
	private String boardShadowValue = "";
	private String[][] board = null;
	private boolean[][] shadowBoard = null;
	
	//Game
	private String[] playerNames = new String[2];
	private int[] playerPoints = new int[2];
	
	//View
	private final Scanner input = new Scanner(System.in);
	

	public static void main(String[] args) {
		System.out.println("A simple Memory Game");
		System.out.println("--------------------");
		System.out.println("");
		
		final Memory game = new Memory();
		game.play(10);
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
		drawBoard(this.board, this.shadowBoard);
		draw(LINE_END);
		draw("Type (q)uit to quit game");
		draw(LINE_END);
		draw(LINE_END);
	}
	
	private void drawPlayerInCharge(int player) {
		drawPlayerIndex(player);
		draw(playerNames[player] + "'s guess: ");
		draw(LINE_END);	
	}
	
	public void play(int boardWith) {
		
		boolean run = true;
		
//		while(run) {
//			
//		}
		
		drawGameIntro();		
		initGame(boardWith);
		
		for (int player = 0; player < playerNames.length; player++) {			
			
			drawGameStats();
			drawPlayerInCharge(player);					
			
			final int[] row = new int[CARD_SIBLINGS];
			final int[] col = new int[CARD_SIBLINGS];
			String card = null;
			boolean match = false;
			
			for(int cardIndex = 0; cardIndex < CARD_SIBLINGS; cardIndex++) {
				
				String input = readString("Row[" + cardIndex + "]: ");
				
				if(drawAskQuit(isQuit(input))) {
					run = false;
					return;
				}
				
				row[cardIndex] = Integer.valueOf(input);
				
				input = readString("Col[" + cardIndex + "]: ");

				if(drawAskQuit(isQuit(input))) {
					run = false;
					return;
				}
				
				col[cardIndex] = Integer.valueOf(input);
				
				setShadowBoard(row[cardIndex], col[cardIndex], SHOW_CARDS);
				
				drawBoard(this.board, this.shadowBoard);
				
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
				drawHint("Sorry, no match  (Enter to Continue)");
			}
			
			if(player + 1 >= playerNames.length) {
				player = 0;
			}
			
			if(cardsLeft() > 0) {
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
			
			if(drawAskYesNo(true, "Do you want to play again? (y/n)")) {
				continue;
			}
			
			run = false;
		}
	}
	
	private void setShadowBoard(int row, int col, boolean mask) {
		shadowBoard[row][col] = mask;
	}
	
	private void resetShadowBoard(int[] row, int[] col) {
		
		for(int i = 0; i < row.length; i++) {
			setShadowBoard(row[i], col[i], !SHOW_CARDS);
		}
	}
	
	private int cardsLeft() {
		return boardElements - cardsRevealed();
	}
	
	private int cardsRevealed() {
		
		int sum = 0;
		
		for(int i = 0; i < playerPoints.length; i++) {
			sum += playerPoints[i];
		}
		
		return sum * cardMatchAmount;		
	}
	
	private boolean stopCompare(String first, String second) {
		
		if(first == null || first.equals(second)) {
			return false;
		}
		
		return true;
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
			
			final String input = readString("Player " + playerNumber + ": ");
			
			if(drawAskYesNo(isExit(input), "Do you really want to exit? (y/n)")){
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
		
		this.board = new String[width][width * cardMatchAmount];
		this.shadowBoard = new boolean[width][width * cardMatchAmount];
		
		initBoardElements(board);
		
		final String[] cards = shuffleCards(getCards(board));  
		
		for(int row = 0, card = 0; row < board.length; row ++) {
			
			for(int col = 0; col < board[row].length; col++, card++) {
				board[row][col] = cards[card];
				shadowBoard[row][col] = !SHOW_CARDS;
			}
		}
	}
	
	//View
		
	private String readString(String text) {
		
		drawNotEmpty(text);
		return input.next();		
	}
	
	private void drawHint(String hint) {
		
		drawNotEmpty(hint);
		
		try {
			System.in.read();
		} catch (IOException e) {
			draw("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private void drawNotEmpty(String text) {
		if (text != null && !text.isEmpty()) {
			draw(text);
		}
	}
	
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
	
	private boolean drawAskQuit(boolean isQuit) {		
		return drawAskYesNo(isQuit, "Do you really want to quit? (y/n)");		
	}
	
	private boolean drawAskYesNo(boolean isCommand, String question) {
		
		if(!isCommand) {
			return false;
		}
		
		return isYes(readString(question));		
	}
	
	private void draw(String text) {
		System.out.print(text);
	}
	
	private void drawBoard(final String[][] board, final boolean[][] shadowBoard) {
		drawBoardHeader();
		draw(LINE_END);
		drawBoardRows(board, shadowBoard);
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
	
	private void drawBoardRow(final String[] cols, final boolean[] shadowCols) {
		drawBoardLine();
		draw(LINE_END);
		drawBoardCols(cols, shadowCols);
		return;
	}
	
	private void drawBoardLine() {
		for(int i = 0; i < this.boardRowWith; i++) {
			draw(ROW_SIGN);
		}
	}
	
	private void drawBoardHeader() {		
		for(int i = 0; i < this.maxRowElements; i++) {
			drawBoardCol(String.valueOf(i), true);
		}
		
		draw(COL_SIGN);
	}
	
	private void drawBoardCols(String[] cols, boolean[] shadowCols) {
		
		for(int i = 0; i < cols.length; i++) {
			drawBoardCol(cols[i], shadowCols[i]);
		}
		
		return;
	}
	
	private void drawBoardCol(String colValue, boolean unmask) {
		draw(COL_SIGN);
		draw(COL_FILLER);
		drawCardValue(unmask ? colValue : boardShadowValue);
		draw(COL_FILLER);
	}
	
	private void drawCardValue(String colValue) {
		
		String cardValue = "";
		
		for(int i = 0; i < this.boardCardWith; i++) {
			
			if( this.boardCardWith <= i + colValue.length()) {
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
	
	private void initBoardColWith( final String[][] board) {
		
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
		
		this.boardCardWith = maxColWith;
		this.boardColWith = maxColWith + (COL_FILLER.length() *2);
		initShadowValue(maxColWith);
	}
	
	private void initShadowValue(int colWith) {
		for(int i = 0; i< colWith; i++) {
			this.boardShadowValue += String.valueOf(SHADOW_SIGN);
		}
	}
	
	private void initBoardMaxRowElements( final String[][] board) {
				
		int maxRowElements = 0;
		
		for (int row = 0; row < board.length; row++) {

			final int rowElements = board[row].length;
			
			if(maxRowElements < rowElements) {
				maxRowElements = rowElements;
			}			
		}
		
		this.maxRowElements = maxRowElements;
	}
	
	private void initBoardElements(String[][] board) {
		
		int boardElements = 0;
		
		for(String[] row : board) {
			boardElements += row.length;
		}
		
		this.boardElements = boardElements;
	}
	
	private void initBoardRowWith( final String[][] board) {
		initBoardColWith(board);
		initBoardMaxRowElements(board);
		this.boardRowWith = (maxRowElements * ROW_SIGN.length() * (boardColWith + COL_SIGN.length())) + COL_SIGN.length();		
	}
			
	private void initBoard(int boardWith) {
		intBoards(boardWith);		
		initBoardRowWith(board);
	}
	
	//Controller
	
	private ERR_CODE isInBoardRange(final int row, final int col) {
		
		if(board.length >= row) {
			return ERR_CODE.ERR_ROW_NOT_FOUND;
		}
		
		if(board[row].length >= col) {
			return ERR_CODE.ERR_COL_NOT_FOUND;
		}	
		
		return ERR_CODE.OK;
	}
	
	private String[] getCards(String[][] board) {	
		final String[] cards = new String[boardElements];
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
			board[row][col] = value;
		}			
		
		return check;
	}
	
	private String getCol(final int row, final int col) {
		return board[row][col];
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
