package client;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.event.MouseAdapter;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import protocol.Game;
import protocol.GameStatus;
import protocol.Player;

public class GoBangClient extends JFrame {

	private static final long serialVersionUID = 1L;
	private static Image memImage = null;
	
	private static final int nx = Game.nx; // 列数
	private static final int ny = Game.ny; // 行数
	private static final int gor = 10;
	
	private Rectangle r = new Rectangle(50, 75, 700, 500);
	private double dx = (double)r.width / nx;
	private double dy = (double)r.height / ny;

	private IOSocket io;
	private Player data[][];

	public GoBangClient() {
		
		io = new IOSocket();
		
		GoBangClient window = this;
		io.register_update_hook(new UpdateHook() {
			@Override
			public void update() {
				repaint();
				switch (io.status) {
				case Connecting:
					setTitle("五子棋 - 正在连接服务器");
					break;
				case CannotConnectServer:
					JOptionPane.showMessageDialog(window, "无法连接到服务器，即将退出！", "无法连接服务器", JOptionPane.INFORMATION_MESSAGE);
					System.exit(-1);
					break;
				case Disconnected:
					JOptionPane.showMessageDialog(window, "断开与服务器的连接，即将退出！", "断开连接", JOptionPane.INFORMATION_MESSAGE);
					System.exit(-1);
					break;
				case Matching:
					setTitle("五子棋 - 匹配中");
					break;
				case WaitGo:
					setTitle("五子棋 - 我方落子");
					break;
				case WaitOtherGo:
					setTitle("五子棋 - 对方落子");
					break;
				case WaitOtherStart:
					setTitle("五子棋 - 等待对方准备");
					break;
				case WaitStart:
					setTitle("五子棋 - 等待游戏开始");
					break;
				case Over:
					setTitle("五子棋 - 游戏结束");
					String winner = "黑方胜";
					if (io.game.winner == Player.Blue)
						winner = "蓝方胜";
					if (io.game.winner == Player.Other)
						winner = "平局";
					JOptionPane.showMessageDialog(window, winner + "！点击屏幕重新开始", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
					io.status = GameStatus.WaitStart;
					break;
				case OtherDisconnect:
					setTitle("五子棋 - 正在重新匹配");
					JOptionPane.showMessageDialog(window, "对方已退出游戏，正在重新匹配！", "对方退出", JOptionPane.INFORMATION_MESSAGE);
					io.game.clear();
					io.status = GameStatus.Rematching;
					break;
				case Rematching:
					setTitle("五子棋 - 正在重新匹配");
					break;
				}
			}
		});
			
		io.start();
		data = io.game.board;
		init();
	}
	
	private void init() {
		setSize(800, 625);
		setLocation(200, 50);
		setResizable(false);
		setTitle("五子棋 - 正在连接服务器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (io.status == GameStatus.WaitStart)
					io.restart();
				if (io.status != GameStatus.WaitGo)
					return;
				Point p = e.getPoint();
				if (p.x + dx/2 < r.x || p.y + dy/2 < r.y)
					return;
				int x = (int)((p.x - r.x + dx/2) / dx);
				int y = (int)((p.y - r.y + dy/2) / dy);
				if (x > nx || y > ny)
					return;
				if (data[x][y] != Player.None)
					return;
				io.Go(x, y);
			}
		});
	}

	public void DrawGo(Graphics2D g) {
		for (int i=0;i<=nx;++i) {
			for (int j=0;j<=ny;++j) {
				if (data[i][j] == Player.None)
					continue;
				else if (data[i][j] == Player.Black)
					g.setColor(Color.black);
				else
					g.setColor(Color.blue);
				g.fill(new Ellipse2D.Double(r.x+i*dx-gor, r.y+j*dy-gor, gor*2, gor*2));
			}
		}
	}

	public void DrawBoard(Graphics2D g) {
		g.setColor(Color.black);
		for (int i=0;i<=nx;++i)
			g.draw(new Line2D.Double(r.x+i*dx, r.y, r.x+i*dx, r.y+r.height));
		for (int i=0;i<=ny;++i)
			g.draw(new Line2D.Double(r.x, r.y+i*dy, r.x+r.width, r.y+i*dy));
	}

	@Override
	public void paint(Graphics g) {
		if (memImage == null)
			memImage = this.createImage(getWidth(), getHeight());
		Graphics2D g2 = (Graphics2D) memImage.getGraphics();
		g2.setColor(Color.lightGray);
		g2.fillRect(0, 0, getWidth(), getHeight());
		super.paint(g2);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		DrawBoard(g2);
		DrawGo(g2);
		if (io.game.last.player != Player.None) {
			g2.setColor(Color.red);
			int i = io.game.last.x;
			int j = io.game.last.y;
			g2.draw(new Ellipse2D.Double(r.x+i*dx-gor, r.y+j*dy-gor, gor*2, gor*2));
		}
		g.drawImage(memImage, 0, 0, null);
	}
}