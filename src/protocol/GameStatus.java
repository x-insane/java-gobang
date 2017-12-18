package protocol;

public enum GameStatus {
	Connecting,
	CannotConnectServer,
	Matching, // 正在寻找对局
	WaitStart, // 等待准备
	WaitOtherStart, // 等待对方准备
	WaitGo,
	WaitOtherGo,
	Over,
	OtherDisconnect,
	Rematching,
	Disconnected
//	Win, Lost, Draw;
}
