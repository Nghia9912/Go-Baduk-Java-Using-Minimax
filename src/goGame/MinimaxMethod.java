package goGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MinimaxMethod {
	public static final int DEPTH = 3; //change the Depth of the Minimax right here
	public static final boolean USE_ALPHA_BETA = true; //turning alphabeta on or off
	static int boardLineCount = 17;
	int userFlag;
	int testInt[][] = new int[boardLineCount][boardLineCount];
	int testStones[][] = new int[200][2];
	int testSteps = 0;
	int comWin = 0;
	int liberty[][] = new int[boardLineCount][boardLineCount];
	int pointInt[][] = new int[boardLineCount][boardLineCount];
	int round = 1;
	boolean pass = false;// ture means one color pass
	boolean isKo = false;// ture is Ko, false is not

	public void test() {
		int i, n = 200;
		for (i = 0; i < n; i++) {
			Point tp = getComputerPoint();
			if (tp != null) {
				putOnTestBoard(tp);
				userFlag = userFlag * -1;
			} else {
				if (pass) {
					break;
				}
				pass = true;
				testStones[testSteps][0] = 0;
				testStones[testSteps][1] = 0;
				testSteps++;
				userFlag = userFlag * -1;
			}
		}
		finish();
	}

	private int heuristic() {
		int score = 0;
		for (int i = 4; i < testInt.length - 4; i++) {
			for (int j = 4; j < testInt[i].length - 4; j++) {
				if (testInt[i][j] == 1) {
					score += 10 + liberty[i][j];
					score += calculateTerritory(i, j, 1);
					score += evaluateGroupStrength(i, j, 1);
				}
				if (testInt[i][j] == -1) {
					score -= 10 + liberty[i][j];
					score -= calculateTerritory(i, j, -1);
					score -= evaluateGroupStrength(i, j, -1);
				}
			}
		}
		return score;
	}

	private int evaluateGroupStrength(int x, int y, int flag) {

		int groupScore = liberty[x][y];
		int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		for (int[] dir : directions) {
			int nx = x + dir[0];
			int ny = y + dir[1];
			if (nx >= 4 && nx < boardLineCount - 4 && ny >= 4 && ny < boardLineCount - 4) {
				if (testInt[nx][ny] == flag) {
					groupScore += 5;
				}
			}
		}
		return groupScore;
	}

	private int calculateTerritory(int x, int y, int flag) {
		int territoryScore = 0;
		int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

		for (int[] dir : directions) {
			int nx = x + dir[0];
			int ny = y + dir[1];

			if (nx >= 4 && nx < boardLineCount - 4 && ny >= 4 && ny < boardLineCount - 4) {
				if (testInt[nx][ny] == 0) {
					for (int[] subDir : directions) {
						int nnx = nx + subDir[0];
						int nny = ny + subDir[1];
						if (nnx >= 4 && nnx < boardLineCount - 4 && nny >= 4 && nny < boardLineCount - 4) {
							if (testInt[nnx][nny] == flag) {
								territoryScore += 5;
							} else if (testInt[nnx][nny] == -flag) {
								territoryScore -= 5;
							}
						}
					}
				}
			}
		}

		int centerX = boardLineCount / 2;
		int centerY = boardLineCount / 2;
		int distanceFromCenter = Math.abs(x - centerX) + Math.abs(y - centerY);
		territoryScore -= distanceFromCenter;

		return territoryScore;
	}

	private Point getComputerPoint() {
		return getBestMove(DEPTH, USE_ALPHA_BETA);
	}
//minimax
	private int minimax(int depth, boolean isMaximizingPlayer) {
		if (depth == 0) {
			return heuristic();
		}

		ArrayList<Point> possibleMoves = getPossibleMoves();


		possibleMoves.sort((a, b) -> {
			int scoreA = evaluateMove(a.x, a.y, isMaximizingPlayer ? 1 : -1);
			int scoreB = evaluateMove(b.x, b.y, isMaximizingPlayer ? 1 : -1);
			return scoreB - scoreA;
		});

		if (isMaximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			for (Point move : possibleMoves) {
				makeMove(move, 1);
				int eval = minimax(depth - 1, false);
				undoMove(move);
				maxEval = Math.max(maxEval, eval);
			}
			return maxEval;
		} else {
			int minEval = Integer.MAX_VALUE;
			for (Point move : possibleMoves) {
				makeMove(move, -1);
				int eval = minimax(depth - 1, true);
				undoMove(move);
				minEval = Math.min(minEval, eval);
			}
			return minEval;
		}
	}
	//minimax alpha beta
	private int alphaBeta(int depth, boolean isMaximizingPlayer, int alpha, int beta) {
		if (depth == 0) {
			return heuristic();
		}

		ArrayList<Point> possibleMoves = getPossibleMoves();

		possibleMoves.sort((a, b) -> {
			int scoreA = evaluateMove(a.x, a.y, isMaximizingPlayer ? 1 : -1);
			int scoreB = evaluateMove(b.x, b.y, isMaximizingPlayer ? 1 : -1);
			return scoreB - scoreA;
		});

		if (isMaximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			for (Point move : possibleMoves) {
				makeMove(move, 1);
				int eval = alphaBeta(depth - 1, false, alpha, beta);
				undoMove(move);
				maxEval = Math.max(maxEval, eval);
				alpha = Math.max(alpha, eval);
				if (beta <= alpha) break;
			}
			return maxEval;
		} else {
			int minEval = Integer.MAX_VALUE;
			for (Point move : possibleMoves) {
				makeMove(move, -1);
				int eval = alphaBeta(depth - 1, true, alpha, beta);
				undoMove(move);
				minEval = Math.min(minEval, eval);
				beta = Math.min(beta, eval);
				if (beta <= alpha) break;
			}
			return minEval;
		}
	}

	private ArrayList<Point> getPossibleMoves() {
		ArrayList<Point> moves = new ArrayList<>();
		for (int i = 4; i < testInt.length - 4; i++) {
			for (int j = 4; j < testInt[i].length - 4; j++) {
				if (testInt[i][j] == 0 && checkAvailable(i, j, userFlag)) {
					moves.add(new Point(i, j));
				}
			}
		}


		moves.sort((a, b) -> {
			int scoreA = evaluateMove(a.x, a.y, userFlag);
			int scoreB = evaluateMove(b.x, b.y, userFlag);
			return scoreB - scoreA;
		});
		return moves;
	}

	private int evaluateMove(int x, int y, int flag) {
		int score = 0;
		score += checkEat(x, y, flag).size() * 10;


		score += calculateTerritory(x, y, flag);

		int center = boardLineCount / 2;
		score -= Math.abs(x - center) + Math.abs(y - center);

		return score;
	}

	public Point getBestMove(int depth, boolean useAlphaBeta) {
		int bestValue = Integer.MIN_VALUE;
		Point bestMove = null;

		ArrayList<Point> possibleMoves = getPossibleMoves();

		for (Point move : possibleMoves) {
			makeMove(move, userFlag);

			int moveValue;
			if (useAlphaBeta) {
				moveValue = alphaBeta(depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE); // Gọi Minimax Alpha-Beta
			} else {
				moveValue = minimax(depth - 1, false);
			}

			undoMove(move);

			if (moveValue > bestValue) {
				bestValue = moveValue;
				bestMove = move;
			}
		}
		return bestMove;
	}


	private void makeMove(Point move, int flag) {
		testInt[move.x][move.y] = flag;
		testStones[testSteps][0] = move.x;
		testStones[testSteps][1] = move.y;
		testSteps++;
	}

	private void undoMove(Point move) {
		testInt[move.x][move.y] = 0;
		testSteps--;
	}

	private ArrayList<Point> checkEat(int i, int j, int userFlag) {
		ArrayList<Point> stonesDead = new ArrayList<>();
		int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

		for (int[] dir : directions) {
			int a = i + dir[0];
			int b = j + dir[1];
			if (testInt[a][b] == -userFlag) {
				ArrayList<Point> temp = findEatPart(a, b, userFlag);
				if (temp != null) {
					for (Point p : temp) {
						if (!stonesDead.contains(p)) {
							stonesDead.add(p);
						}
					}
				}
			}
		}
		return stonesDead;
	}

	private boolean checkAvailable(int i, int j, int userFlag) {
		boolean[][] visited = new boolean[boardLineCount][boardLineCount];
		Queue<Point> queue = new LinkedList<>();
		int countLiberty = 0;

		queue.add(new Point(i, j));
		visited[i][j] = true;
		testInt[i][j] = userFlag;
		stoneLiberty();

		while (!queue.isEmpty()) {
			Point point = queue.poll();
			int x = point.x, y = point.y;

			int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
			for (int[] dir : directions) {
				int newX = x + dir[0], newY = y + dir[1];
				if (newX >= 0 && newX < boardLineCount && newY >= 0 && newY < boardLineCount) {
					if (testInt[newX][newY] == userFlag && !visited[newX][newY]) {
						queue.add(new Point(newX, newY));
						visited[newX][newY] = true;
					}
				}
			}

			// Cộng liberty
			countLiberty += liberty[x][y];
		}

		if (countLiberty == 0) {
			testInt[i][j] = 0;
			return false;
		}
		return true;
	}

	private ArrayList<Point> findEatPart(int a, int b, int userFlag) {
		ArrayList<Point> stonesVisited = new ArrayList<>();
		Queue<Point> queue = new LinkedList<>();
		boolean[][] visited = new boolean[boardLineCount][boardLineCount];
		int countLiberty = 0;

		queue.add(new Point(a, b));
		visited[a][b] = true;

		while (!queue.isEmpty()) {
			Point current = queue.poll();
			stonesVisited.add(current);
			int x = current.x, y = current.y;


			int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
			for (int[] dir : directions) {
				int newX = x + dir[0], newY = y + dir[1];

				if (newX >= 0 && newX < boardLineCount && newY >= 0 && newY < boardLineCount) {

					if (testInt[newX][newY] == -userFlag && !visited[newX][newY]) {
						queue.add(new Point(newX, newY));
						visited[newX][newY] = true;
					}

					else if (testInt[newX][newY] == 0) {
						countLiberty++;
					}
				}
			}
		}


		if (countLiberty < 1 && !(stonesVisited.size() == 1 && a == testStones[testSteps - 1][0]
				&& b == testStones[testSteps - 1][1] && isKo)) {
			return stonesVisited;
		}

		return null;
	}

	private void stoneLiberty() {
		int boardSize = testInt.length;
		for (int a = 4; a < boardSize - 4; a++) {
			for (int b = 4; b < boardSize - 4; b++) {
				if (testInt[a][b] == 0) {
					liberty[a][b] = 0;
				} else {
					liberty[a][b] = calculateLiberty(a, b, boardSize);
				}
			}
		}
	}

	private int calculateLiberty(int x, int y, int boardSize) {
		int libertyCount = 0;

		int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		for (int[] dir : directions) {
			int newX = x + dir[0];
			int newY = y + dir[1];
			if (newX >= 4 && newX < boardSize - 4 && newY >= 4 && newY < boardSize - 4) {
				if (testInt[newX][newY] == 0) {
					libertyCount++;
				}
			}
		}
		return libertyCount;
	}

	private void putOnTestBoard(Point tp) {
		int x = tp.x;
		int y = tp.y;

		ArrayList<Point> capturedStones = checkEat(x, y, userFlag);
		pass = false;

		isKo = capturedStones.size() == 1 && liberty[x][y] == 0;

		updateTestBoard(x, y, capturedStones);
	}

	private void updateTestBoard(int x, int y, ArrayList<Point> capturedStones) {
		testInt[x][y] = userFlag;

		if (!capturedStones.isEmpty()) {
			eat(x, y, capturedStones);
		}


		testStones[testSteps][0] = x;
		testStones[testSteps][1] = y;
		testSteps++;
	}

	private void eat(int x, int y, ArrayList<Point> stonesDead) {
		for (Point deadPoint : stonesDead) {
			testInt[deadPoint.x][deadPoint.y] = 0;
		}
	}

	private void countPoint() {
		ArrayList<Point> stonesVisited = new ArrayList<>();
		int blackPoint = 0, whitePoint = 1;

		for (int i = 4; i < 13; i++) {
			for (int j = 4; j < 13; j++) {
				Point currentPoint = new Point(i, j);


				if (stonesVisited.contains(currentPoint))
					continue;

				if (testInt[i][j] == 0) {

					ArrayList<Point> territory = checkState(i, j);
					for (Point p : territory) {
						if (!stonesVisited.contains(p)) {
							stonesVisited.add(p);
						}
					}
				} else {

					stonesVisited.add(currentPoint);
					pointInt[i][j] = testInt[i][j];
				}
			}
		}

		for (int i = 4; i < 13; i++) {
			for (int j = 4; j < 13; j++) {
				if (pointInt[i][j] == 1)
					blackPoint++;
				else if (pointInt[i][j] == -1)
					whitePoint++;
			}
		}

		if (blackPoint < whitePoint)
			comWin++;
	}

	private ArrayList<Point> checkState(int a, int b) {
		ArrayList<Point> stonesVisited = new ArrayList<>();
		int boradState = 2;
		int findCount;
		int begin = 0, end = 0;
		int newx, newy;
		stonesVisited.add(new Point(a, b));

		do {
			findCount = 0;
			for (int i = begin; i <= end; i++) {
				Point currentPoint = stonesVisited.get(i);
				newx = currentPoint.x;
				newy = currentPoint.y;


				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						if (Math.abs(dx) + Math.abs(dy) == 1) {
							int nx = newx + dx;
							int ny = newy + dy;


							if (nx >= 0 && nx < boardLineCount && ny >= 0 && ny < boardLineCount) {
								if (testInt[nx][ny] == 0 && !stonesVisited.contains(new Point(nx, ny))) {
									stonesVisited.add(new Point(nx, ny));
									findCount++;
								} else if ((testInt[nx][ny] == 1 || testInt[nx][ny] == -1)
										&& !stonesVisited.contains(new Point(nx, ny))) {

									if (boradState == 2) {
										boradState = testInt[nx][ny];
									} else if (boradState == -testInt[nx][ny]) {
										boradState = 0;
									}
								}
							}
						}
					}
				}

			}
			begin = end + 1;
			end = end + findCount;
		} while (findCount > 0);

		for (Point point : stonesVisited) {
			pointInt[point.x][point.y] = boradState;
		}

		return stonesVisited;
	}

	private void finish() {
		countPoint();
		round++;
	}

	private void begin() {
		userFlag = 1;
		testSteps = 0;
		testStones = null;
		testStones = new int[200][2];
		pass = false;
		isKo = false;
		for (int i = 0; i < testInt.length; i++) {
			for (int j = 0; j < testInt[i].length; j++) {
				if (i >= 4 && i < testInt.length - 4 && j >= 4 && j < testInt.length - 4) {
					testInt[i][j] = 0;
				} else {
					testInt[i][j] = 10;
				}
			}
		}
	}

	public void setBoard(int[][] board, int[][] stones, int steps, int userFlag) {
		this.userFlag = userFlag;
		this.testSteps = steps;

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				testInt[i][j] = board[i][j];
			}
		}

		for (int i = 0; i < steps; i++) {
			testStones[i][0] = stones[i][0];
			testStones[i][1] = stones[i][1];
		}
	}

	public void minimaxMethod(int depth) {
		begin();
		while (round <= 1) {
			Point bestMove = getBestMove(DEPTH, USE_ALPHA_BETA);
			if (bestMove != null) {
				putOnTestBoard(bestMove);
				userFlag = -userFlag;
			} else {
				pass = true;
				break;
			}
		}
		finish();
	}

}
