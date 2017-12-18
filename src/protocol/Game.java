package protocol;

public class Game {
	
	public class Last {
		public int x = -1, y = -1;
		public Player player = Player.None;
		public void update(int x, int y, Player p) {
			this.x = x;
			this.y = y;
			this.player = p;
		}
	}
	
	public static final int nx = 27;
	public static final int ny = 18;
	
	public Last last = new Last();
	public Player[][] board = new Player[nx+1][ny+1];
	public Player winner = Player.None;
	
	public Game() {
		clear();
	}
	
	public synchronized void clear() {
		for (int i=0;i<=nx;i++)
			for (int j=0;j<=ny;j++)
				board[i][j] = Player.None;
		last.update(-1, -1, Player.None);
		winner = Player.None;
	}
	
	public synchronized boolean play(int x, int y, Player p) {
		if (board[x][y] != Player.None)
			return false;
		board[x][y] = p;
		last.update(x, y, p);
		winner = get_winner();
		return true;
	}
	
	private synchronized Player get_winner() {
		if (last.player == Player.None)
			return Player.None;
		for (int i=0;i<=1;i++) {
			for (int j=-1;j<=1;j++) {
				if (i==0 && j==0)
					continue;
				if (i==0 && j==-1)
					continue;
				int n = 1;
				int px = last.x;
				int py = last.y;
				while(true) {
					px += i;
					py += j;
					if (px <= nx && py >= 0 && py <= ny && board[px][py] == last.player)
						n ++;
					else
						break;
				}
				px = last.x;
				py = last.y;
				while(true) {
					px -= i;
					py -= j;
					if (px >= 0 && py >= 0 && py <= ny && board[px][py] == last.player)
						n ++;
					else
						break;
				}
				if (n >= 5)
					return last.player;
			}
		}
		for (int i=0;i<=nx;i++)
			for (int j=0;j<=ny;j++)
				if (board[i][j] == Player.None)
					return Player.None;
		return Player.Other;
	}
}
