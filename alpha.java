import java.awt.Point;
import java.util.ArrayList;



public class alpha extends AIModule
{
	int[] prior = {3,1,5,2,4,6,0}; //think first
	private static int player;
	private static int opponent;
	private static boolean FLAG = false;
	int temp;
	int best = 0;
	int maxDepth;
	
	public class four //for all possible fours in graph
	{
		Point[] points = new Point[4];
		
		public four(Point p1, Point p2, Point p3, Point p4)
		{
			points[0] = p1;
			points[1] = p2;
			points[2] = p3;
			points[3] = p4;
		}
	}
	
	public class pair
	{
		Point[] points = new Point[2];
		
		public pair(Point p1, Point p2)
		{
			points[0] = p1;
			points[1] = p2;
		}
	}
	
	final ArrayList<four> fours = new ArrayList<four>();
	
	public alpha()
	{
		for (int row = 0;row < 6;row++) //horizontal
			for (int col = 0;col<4;col++)
				fours.add(new four(new Point(row, col), new Point(row, col+1), new Point(row, col+2), new Point(row,col+3)));
		
		for (int row = 0;row < 3;row++) //vertical
			for (int col = 0; col < 7; col++)
				fours.add(new four(new Point(row, col), new Point(row+1, col), new Point(row+2, col), new Point(row+3,col)));
		
		for (int row = 0;row < 3;row++) //diagonal +
			for (int col = 0; col < 4; col++)
				fours.add(new four(new Point(row, col), new Point(row+1, col+1), new Point(row+2, col+2), new Point(row+3,col+3)));
		
		for (int row = 3;row < 6;row++) //diagonal -
			for (int col = 0; col < 4; col++)
				fours.add(new four(new Point(row, col), new Point(row-1, col+1), new Point(row-2, col+2), new Point(row-3,col+3)));

	}
	
	private static boolean control;
	public int eva(final GameStateModule game) //who control the game
	{
		ArrayList<Point> p3 = check3(game, player);
		ArrayList<Point> o3 = check3(game, opponent);
		
		ArrayList<pair> p2 = check2(game, player);
		ArrayList<pair> o2 = check2(game, opponent);
		
		int result = 0;
		
		if (player==1)
		{
			if (p3.size()==0) //loss
			{
				for (Point b: o3)
					if (b.y % 2 == 1)
						return -1000000;
			}
			else if (o3.size()==0)
			{
				for (Point w: p3)
					if (w.y % 2 == 0)
						return +1000000;
			}
			if (!control)
			{
				for (Point p:p3)
					if (p.y%2==0)
					{
						result += 2000;					
						for (Point o:o3)
							if (o.x==p.x && o.y < p.y)
								result -= 2000;
					}
				for (Point p:o3)
					if (p.y%2==1)
						result -= 2000;
			}
			else
			{
				result = 1000;
				
				boolean lost = true;
				for (Point w:p3)
					if (w.y % 2 == 0)
					{
						lost = false;
						for (Point o:o3)
							if (o.x==w.x && o.y < w.y)
								result -= 2000;
					}
				
				if (lost)
					result -= 2000;
			}
		}
		else
		{
			if (p3.size()==0) //loss
			{
				for (Point w: o3)
					if (w.y % 2 == 0)
						return -1000000;
			}
			else if (o3.size()==0)
			{
				for (Point b: p3)
					if (b.y % 2 == 1)
						return +1000000;
			}
			
			if(!control)
			{
				for (Point o:o3)
					if (o.y%2==0)
					{
						result -= 2000;					
						for (Point p:o3)
							if (o.x==p.x && p.y < o.y)
								result += 2000;
					}
			}
			else
			{
				result = 1000;
				for (Point b:p3)
					if (b.y%2==1)
						result+=2000;
			}
		}
		
		return result;
		
	}
	
    public void getNextMove(final GameStateModule game) 
    {
    	 	
    	if(!FLAG)
    	{
        	if (game.getActivePlayer()==1) //initial
        	{
        		player = 1;
        		opponent = 2;
        	}
        	else
        	{
        		player = 2;
        		opponent = 1;
        	}
        	
			FLAG = true;			
    	}
    	
		ArrayList<Point> p3 = check3(game, player);
		ArrayList<Point> o3 = check3(game, opponent);
		
    	if (player == 1) //Do i have control right now?
    	{
    		control = false;
    		for (Point w:p3)
    			if (w.y%2==0)
    				control = true;
    	}
    	else
    	{
    		control = true;
    		for (Point w:o3)
    			if (w.y%2==0)
    				control = false;
    	}
			maxDepth = 1;
    	
			while(!terminate && maxDepth < (42 - game.getCoins()))
			{
				alphaBeta(Integer.MIN_VALUE, Integer.MAX_VALUE, game, maxDepth, true);
    		
				if (!terminate) //if miniMax doesn't have time to think clearly, don't let it choose move. Otherwise terrible
					chosenMove = temp;
    		
				maxDepth++;
			}  
		
    }
    
    private int alphaBeta(int alpha, int beta, final GameStateModule game, int depth, boolean turn)
    {
    	if (terminate || game.getCoins()==42)
    		return 0;
        if (depth==0)
        {
        	return eva(game);
        }

    	
        for (int move : prior)
            if (game.canMakeMove(move))
            {
                game.makeMove(move);
                
                if (game.isGameOver())
                {
                	int winner = game.getWinner();
                	best = winner==0 ? 0 : (winner==player ? Integer.MAX_VALUE : Integer.MIN_VALUE);
                }
                else
                	best = alphaBeta(alpha, beta, game, depth-1, !turn);
                
                if(turn)
                {
                	if (alpha < best)
                	{
                		alpha = best;
                		if (depth==maxDepth) //choose the best move.
                			temp = move;
                	}
                }
                else
                	if (beta > best)
                		beta = best;
                	
                game.unMakeMove();
                
                if (beta <= alpha)
                	break;
            }
        return turn?alpha:beta;
    }
    
    public ArrayList<Point> check3(final GameStateModule game, final int p)
    {
    	final ArrayList<Point> empty = new ArrayList<Point>();

    	final int[] buffer = new int[4];
    	
		for (int i=0;i<fours.size();i++)
		{
			four temp = fours.get(i);
			buffer[0] = game.getAt(temp.points[0].x, temp.points[0].y);
			buffer[1] = game.getAt(temp.points[1].x, temp.points[1].y);
			buffer[2] = game.getAt(temp.points[2].x, temp.points[2].y);
			buffer[3] = game.getAt(temp.points[3].x, temp.points[3].y);
				
			if (buffer[0]==p && buffer[1]==p && buffer[2]==p && buffer[3]==0)
				empty.add(temp.points[3]);
			else if (buffer[0]==p && buffer[1]==p && buffer[2]==0 && buffer[3]==p)	
				empty.add(temp.points[2]);
			else if (buffer[0]==p && buffer[1]==0 && buffer[2]==p && buffer[3]==p)	
				empty.add(temp.points[1]);
			else if (buffer[0]==0 && buffer[1]==p && buffer[2]==p && buffer[3]==p)	
				empty.add(temp.points[0]);
		}
		
    	return empty;
    }
    
    public ArrayList<pair> check2(final GameStateModule game, final int p)
    {
    	ArrayList<pair> empty = new ArrayList<pair>();

    	final int[] buffer = new int[4];
    	
		for (int i=0;i<fours.size();i++)
		{
			four temp = fours.get(i);
			buffer[0] = game.getAt(temp.points[0].x, temp.points[0].y);
			buffer[1] = game.getAt(temp.points[1].x, temp.points[1].y);
			buffer[2] = game.getAt(temp.points[2].x, temp.points[2].y);
			buffer[3] = game.getAt(temp.points[3].x, temp.points[3].y);
				
			if (buffer[0]==p && buffer[1]==p && buffer[2]==0 && buffer[3]==0) //1100
				empty.add(new pair(temp.points[2], temp.points[3]));
			else if (buffer[0]==p && buffer[1]==0 && buffer[2]==0 && buffer[3]==p)//1001	
				empty.add(new pair(temp.points[1], temp.points[2]));
			else if (buffer[0]==p && buffer[1]==0 && buffer[2]==p && buffer[3]==0)//1010
				empty.add(new pair(temp.points[1], temp.points[3]));
			else if (buffer[0]==0 && buffer[1]==0 && buffer[2]==p && buffer[3]==p)//0011
				empty.add(new pair(temp.points[0], temp.points[1]));
			else if (buffer[0]==0 && buffer[1]==p && buffer[2]==p && buffer[3]==0)	//0110
				empty.add(new pair(temp.points[0], temp.points[3]));
			else if (buffer[0]==0 && buffer[1]==p && buffer[2]==0 && buffer[3]==p)	//0101
				empty.add(new pair(temp.points[0], temp.points[2]));
		}
		
    	return empty;
    }

}
