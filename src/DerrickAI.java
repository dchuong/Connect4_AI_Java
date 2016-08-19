import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Scanner;

public class DerrickAI extends CKPlayer {

	private byte opp;
	private int width;
	private int height;
	double pInf = Double.POSITIVE_INFINITY;
	double nInf = Double.NEGATIVE_INFINITY;
	
	Point center;
	private class Value  {
		private double value;
		private Point point;
		public Value(Point p, double v) {
			this.point = p;
			this.value = v;
		}
		
		public double getValue() {
			return this.value;
		}
		
		public Point getPoint() {
			return this.point;
		}
		
	}
	
	private class ValueCompare implements Comparator<Value> {
		public int compare(Value my, Value other) {
			if (my.getValue() > other.getValue()) 
				return -1;
			else if (my.getValue() == other.getValue())
				return 0;
			return 1;
		}
	}
	
	public DerrickAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "BigPomelo";
		this.width = state.getWidth();
		this.height = state.getHeight();
		this.opp = player == 1 ? (byte) 2 : 1;
		this.center = getCenter(state);
	}
	
	public List<Point> getPossibleMoves(BoardModel state) {
		List<Value> findMoves = new ArrayList<>();
		Point c = getCenter(state);
	
		for (int x = 0 ; x < width; x++) {
			for (int y = 0; y < height; y++) {
				//free space
				if (state.getSpace(x, y) == 0 && 
					(!state.gravityEnabled() || y == 0 || state.getSpace(x, y - 1) != 0)) {
				
					Point move = new Point(x,y);
					
					int newx = c.x - move.x;
					int newy = c.y - move.y;
					double dist = newx * newx + newy * newy;
					findMoves.add(new Value(move, dist));
				}
			}
		
		}
		
		return convertToList(findMoves);

	}
	
	public double heuristic(BoardModel s) {
		double mypoint =0;
		double nmepoint =0;
		double total = 0;
		
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				Point currPoint = new Point(x,y);
				
				if (s.getSpace(x,y) == this.player) {
					double temp = getDist(center, currPoint) + 3;
					mypoint += 5 /temp ;
			
				}
				else if (s.getSpace(x,y) == this.opp){
					double temp = getDist(center, currPoint) + 3;
					nmepoint += 5/ temp;
				}
			}
		}
		total = mypoint - nmepoint;
		double playerW = 0;
		double oppW = 0;
		if (s.winner() == player) {
			playerW = 1;
		}
		else if (s.winner() == opp) {
			oppW = 1;
		}
		double win = playerW - oppW;
		return 300 * win + total;
	}



	 public Point depthlimited(BoardModel state, int time) {
		PriorityQueue <Value> allMove = new PriorityQueue<>();	
		
		long end = System.currentTimeMillis() + time;
		double value = 0;
		int d = 0;
		
		
		while(true) {
			List<Value> depthmove = new ArrayList<Value>();
			List<Point> iterator = getPossibleMoves(state);
			
			for (Point win: iterator) {
				if (state.clone().placePiece(win,player).winner() == player) 
					return win;
			}
			for (Point traverse: iterator) {
				value  = alphabeta(state.placePiece(traverse,player),
						 nInf, pInf, end, d, false);
				if (value != nInf)
					depthmove.add(new Value(traverse, value));
				else 
					return allMove.peek().getPoint();
			}
			// if there is none go deeper
			d++;
			allMove = new PriorityQueue<>(depthmove.size(), new ValueCompare());
			allMove.addAll(depthmove);
		}
	}
	
	public List<Point> convertToList(List<Value> x) {
		
		Collections.sort(x, Collections.reverseOrder(new ValueCompare()));
		
		List<Point> temp = new ArrayList<>();
		for (Value score: x) {
			temp.add(score.getPoint());
		}
		
		return temp;
	}
	
	public double alphabeta(BoardModel state, double a, double b, long timeEnd, int d, boolean myTurn) {
		if (safeTime(50) > timeEnd) 
			return nInf;	
		if (!state.hasMovesLeft() || state.winner() != -1 || d < 1)
			return heuristic(state);
		else {
			List<Point> iterator = getPossibleMoves(state);
			if (myTurn) {
				for (Point traverse: iterator ) {
					double num = alphabeta(state.placePiece(traverse, player),
							 a, b, timeEnd,d - 1, !myTurn);
					a = Math.max(a, num);
					if (a >= b)
						return b;
				}
				return a;		
			}
			else {
				for (Point traverse: iterator) {
					double num = alphabeta(state.placePiece(traverse, opp),
							 a, b, timeEnd, d - 1, !myTurn);
					b = Math.min(b, num);
					if (a >= b)
						return a;
				}
				return b;
			}
		}
	}

	public double safeTime(int x) {
		return System.currentTimeMillis() + x;
	}
	public Point getCenter(BoardModel s) {
		return new Point(width/ 2,height/2);
	}
	
	public double getDist(Point a, Point b){
		int x = a.x - b.x;
		int y = a.y - b.x;
		double num = x*x + y *y;

		return Math.abs(num);
	}
	@Override
	public Point getMove(BoardModel state) {
		System.out.println("Enter a time: ");
		Scanner input = new Scanner(System.in);
		int time = input.nextInt();
		return getMove(state,time);
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		return depthlimited(state, deadline);
	}
}
