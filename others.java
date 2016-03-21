public class others extends AIModule
{
    private int depthLimit;
    private int maxMove;
    private int[] branchOrdering = {3, 2, 4, 1, 5, 0, 6};
    private EvaluationTable ET_Current = new EvaluationTable();
    
//    // Debugging Flags ====================================================================================
//    // ====================================================================================================
//    // print evaluation table after each move
//    boolean printEvaluationTableAtMove = false;
//    // print evaluation table for every leaf
//    boolean printEvaluationTableAtLeaf = false;
//    // print score at every leaf
//    boolean printScoreAtLeaf = false;
//    // print out comma separated leaves for testing! probably turn everything else to false when you use this one.
//    boolean printTestLeaves = false;

    public void getNextMove(final GameStateModule game) {
            
        // update with opponents last move
        ET_Current.opponentsMove(game); 
        
        depthLimit = 0;
        int depthOfChoice = 0;
        while( !terminate ) { // iterative deepening 

            // run alphabeta
            alphaBeta(game, ET_Current, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            if( !terminate ) {
                chosenMove = maxMove;
                depthOfChoice = depthLimit;
            }
            
            // increment max depth for next iteration
            depthLimit++;
        }
        
        // update with our move
        ET_Current.move(game, chosenMove, 1); 
//        System.out.println("chosenMove: " + chosenMove + " at depth: " + depthOfChoice);
        
//        if(printEvaluationTableAtMove)
//            ET_Current.print();
    }

    private int alphaBeta(final GameStateModule game, EvaluationTable ET, int depth, int playerIndex, int alpha, int beta){
        if( terminate ) // get out as quickly as possible, won't use return value
            return 0;
        
        if ( depth == depthLimit ) {
            
//            if(printScoreAtLeaf)
//                System.out.println( "Score: " + ET.getScore() );
//            if(printTestLeaves)
//                System.out.print( ET.getScore() + ",");
//            if(printEvaluationTableAtLeaf) 
//                ET.print();
            
            return ET.getScore();
        }
        depth++;

        if(playerIndex ==  1){ // Max player
            for(int i : branchOrdering )
                if(game.canMakeMove (i))
                {
                    game.makeMove(i);
                    EvaluationTable ET_Copy = new EvaluationTable(ET);
                    ET_Copy.move(game, i, 1);
                    
                    int v = alphaBeta(game, ET_Copy, depth, 2, alpha, beta);
                    if (alpha < v){
                        alpha = v;
                        if( depth == 1 )
                            maxMove = i;
                    }
                    game.unMakeMove();
                    
                    if( beta <= alpha )
                        break;
                }
            return alpha;
         } 
        else { // Min player
            for(int i : branchOrdering)
                if(game.canMakeMove (i)) 
                {
                    game.makeMove(i);
                    EvaluationTable ET_Copy = new EvaluationTable(ET);
                    ET_Copy.move(game, i, 2);
                    
                    int v = alphaBeta(game, ET_Copy, depth, 1, alpha, beta);
                    if (beta > v){
                            beta = v;
                    }
                    game.unMakeMove();
                    
                    if( beta <= alpha )
                        break;
                }
            return beta;
        }
    }

    // print connect-4 board
    private void printBoard(final GameStateModule game) {
        for(int i=5; i>-1; i--) {
            for(int j=0; j<7; j++) 
            {
                if(game.getAt(j,i) == 1)
                    System.out.print(" X");
                else if(game.getAt(j,i) == 2)    
                    System.out.print(" O");
                else   
                    System.out.print(" .");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    // table that allows you to evaluate the board
    public class EvaluationTable {
        private int score;
        private int[][] v; // [rows][columns]
        private int[][] h; 
        private int[][] dp;
        private int[][] dn;
        private final int[][] weights = 
            {{1, 1, 1, 2, 1, 1, 1},	
             {1, 2, 2, 3, 2, 2, 1},
             {1, 2, 3, 4, 3, 2, 1},
             {1, 2, 3, 4, 3, 2, 1},
             {1, 2, 2, 3, 2, 2, 1},		  				  		
             {1, 1, 1, 2, 1, 1, 1}};
        
        public EvaluationTable() {
            score = 0;
            // these are bigger to avoid array out of bounds errors
            v = new int[8][9]; //[rows:1,6][cols:1,7]
            h = new int[8][9]; 
            dp = new int[8][9];
            dn = new int[8][9];
        }
        
        // copy constructor
        public EvaluationTable( EvaluationTable E ) {
            score = E.score;
            
            v = new int[8][9]; //[rows:1,6][cols:1,7]
            h = new int[8][9]; 
            dp = new int[8][9];
            dn = new int[8][9];
            
            for(int i=0; i<8; i++) // update to java array copy later
                for(int j=0; j<9; j++) {
                    this.v[i][j] = E.v[i][j];
                    this.h[i][j] = E.h[i][j];
                    this.dp[i][j] = E.dp[i][j];
                    this.dn[i][j] = E.dn[i][j];
                }
        }
        
        // updates the EvaluationTable based on move
        public void move(final GameStateModule game, int col, int player) {
            
            int p = 0;
            if( player == 1 ) p=1; else p=-1;
            
            col++; // adjust col for our arrays
            int row = 1; // find last move
            while( v[row][col] != 0 )
                row++;
            
            // update vertical, just check below
            int b = p*v[row-1][col];
            if( b > 0 )
                for(int i=0; i<=b; i++)
                    v[row-i][col] = p*b+p;
            else 
                v[row][col] = p;
                
            // update horizontal
            int l = p*h[row][col-1]; // get left
            int r = p*h[row][col+1]; // get right
            
            if( l > 0 && r > 0 ) { // both
                
                for(int i=0; i<=l+r; i++)
                    h[row][col-l+i] = p*(l+r)+p;
                
            } else if( l > 0 ) { // just left
            
                for(int i=0; i<=l; i++)
                    h[row][col-i] = p*l+p;
                
            } else if( r > 0) { // just right
                
                for(int i=0; i<=r; i++)
                    h[row][col+i] = p*r+p;
                
            } else // neither
                h[row][col] = p;
                
            // update diagonal positive
            int bl = p*dp[row-1][col-1]; // get bottom left
            int tr = p*dp[row+1][col+1]; // get top right
            
            if( bl > 0 && tr > 0 ) { // both
                
                for(int i=0; i<=bl+tr; i++)
                    dp[row-bl+i][col-bl+i] = p*(bl+tr)+p;
                
            } else if( bl > 0 ) { // just bottom left
            
                for(int i=0; i<=bl; i++)
                    dp[row-i][col-i] = p*bl+p;
                
            } else if( tr > 0) { // just top right
                
                for(int i=0; i<=tr; i++)
                    dp[row+i][col+i] = p*tr+p;
                
            } else // neither
                dp[row][col] = p;
                
            // update diagonal negative
            int br = p*dn[row-1][col+1]; // get bottom right
            int tl = p*dn[row+1][col-1]; // get top left
            
            if( br > 0 && tl > 0 ) { // both
                
                for(int i=0; i<=br+tl; i++)
                    dn[row-br+i][col+br-i] = p*(br+tl)+p;
                
            } else if( br > 0 ) { // just bottom right
            
                for(int i=0; i<=br; i++)
                    dn[row-i][col+i] = p*br+p;
                
            } else if( tl > 0) { // just top left
                
                for(int i=0; i<=tl; i++)
                    dn[row+i][col-i] = p*tl+p;
                
            } else // neither
                dn[row][col] = p;
            
            
//            // int[8][9]; checks for mistakes, use for debugging only
//            int check = 0;
//            for(int i=0; i<8; i++) { // sum cols 0,8
//                check += v[i][0] + h[i][0] + dp[i][0] + dn[i][0]; 
//                check += v[i][8] + h[i][8] + dp[i][8] + dn[i][8]; 
//            }
//            for(int i=0; i<9; i++) { // sum rows 0,8
//                check += v[0][i] + h[0][i] + dp[0][i] + dn[0][i]; 
//                check += v[7][i] + h[7][i] + dp[7][i] + dn[7][i]; 
//            }
//            if( check != 0 ) {
//                System.out.println("Evaluation Table Error.");
//                print();
//                System.exit(10);
//            }
//            
//            for(int i=0; i<6; i++) {
//                for(int j=0; j<7; j++) {
//                    if( game.getAt(j, i) == 1 ) {
//                        if( v[i+1][j+1] <= 0 || h[i+1][j+1] <= 0 || dp[i+1][j+1] <= 0 || dn[i+1][j+1] <= 0 ) {
//                            System.out.println("Evaluation Table Error 1: " + (i+1) + " " + (j+1));
//                            print();
//                            System.exit(10);
//                        } 
//                    } else if( game.getAt(j, i) == 2 ) {
//                        if( v[i+1][j+1] >= 0 || h[i+1][j+1] >= 0 || dp[i+1][j+1] >= 0 || dn[i+1][j+1] >= 0 ) {
//                            System.out.println("Evaluation Table Error 2: " + (i+1) + " " + (j+1));
//                            print();
//                            System.exit(10);
//                        } 
//                    } else if( game.getAt(j, i) == 0 ) {
////                        if( v[i+1][j+1] != 0 || h[i+1][j+1] != 0 || dp[i+1][j+1] != 0 || dn[i+1][j+1] != 0 ) {
////                            System.out.println("Evaluation Table Error 3: " + (i+1) + " " + (j+1));
////                            print();
//////                            System.exit(10);
////                        } 
//                    }
//                }
//            }
        }
        
        // find and add opponents move to the EvaluationTable
        public void opponentsMove(final GameStateModule game) {
            for(int i=0; i<6; i++) {
                for(int j=0; j<7; j++) {
                    if( game.getAt(j,i) != 0 && v[i+1][j+1] == 0 ) {
                        move(game, j, 2);
                        return;
                    }
                }
            }
        }
        
        // score table by adding up all elements multiplied by that elements value
        private void scoreTable() {
            score = 0;
            for(int i=1; i<=6; i++) 
                for(int j=1; j<=7; j++) {
                    // if connect-4, max out score
                    if( v[i][j] == 4 || h[i][j] == 4 || dp[i][j] == 4 || dn[i][j] == 4 )
                        score += 1000;
                    else if( v[i][j] == -4 || h[i][j] == -4 || dp[i][j] == -4 || dn[i][j] == -4 )
                        score -= 1000;
                    score += (v[i][j] + h[i][j] + dp[i][j] + dn[i][j])*(weights[i-1][j-1]);
                }
        }
        
        public int getScore() {
            scoreTable();
            return score;
        }
        
        // print out the EvaluationTable in four tables
        public void print() {
            int min_i = 0;
            int max_i = 7;
            int min_j = 0;
            int max_j = 8;
            System.out.println( "Vertical" );
            for(int i=max_i; i>=min_i; i--) {
                for(int j=min_j; j<=max_j; j++) 
                {
                    System.out.print( String.format("%2d", v[i][j]) );
                }
                System.out.println();
            }
            System.out.println();
            
            System.out.println( "Horizontal" );
            for(int i=max_i; i>=min_i; i--) {
                for(int j=min_j; j<=max_j; j++) 
                {
                    System.out.print( String.format("%2d", h[i][j]) ); 
                }
                System.out.println();
            }
            System.out.println();
            
            System.out.println( "Diagonal Positive" );
            for(int i=max_i; i>=min_i; i--) {
                for(int j=min_j; j<=max_j; j++) 
                {
                    System.out.print( String.format("%2d", dp[i][j]) );
                }
                System.out.println();
            }
            System.out.println();
            
            System.out.println( "Diagonal Negative" );
            for(int i=max_i; i>=min_i; i--) {
                for(int j=min_j; j<=max_j; j++) 
                {
                    System.out.print( String.format("%2d", dn[i][j]) );
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}

