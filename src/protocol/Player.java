package protocol;

public enum Player {
	None, Black, Blue, Other;

	public static Player op(Player p) {
		if (p == Player.Black)
			return Player.Blue;
		if (p == Player.Blue)
			return Player.Black;
		return Player.None;
	}
	
	public static byte result(Player winner, Player me) {
		if (winner == me)
			return 1;
		if (winner == op(me))
			return -1;
		return 0;
	}
}
