package com.xyq.fs.login;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.apache.lucene.document.Document;

import com.xyq.fs.entity.Page;
import com.xyq.fs.resource.R;
import com.xyq.fs.service.FSService;
import com.xyq.fs.util.MyFilesUtil;
import com.xyq.fs.util.MyHighlightUtil;

public class ResultWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FSService fs = new FSService();
	private Page<Document> page;
	int g = 15;
	// 我机器上是1100
	int you = 900;

	public ResultWindow(Page<Document> page) {

		this.page = page;

		String title = page.getQueryStr();

		// 设置下点右上角的关闭按钮使程序退出，调试方便些
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setTitle("关键词: \"" + title + "\" 共查询到" + page.getTotalRecord()
				+ "条相关文档信息,耗时:" + page.getSearchTime() + "秒,当前页:"
				+ page.getCurrentPage() + "  本页显示:" + page.getDocList().size()
				+ "条 ");
		setSize(1500, 1500);
		// 新建一个JPanel面板panel，上面用来摆东西
		JPanel panel = new JPanel();
		// 在panel上面摆上label
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBounds(100, 100, 800, 500);
		JScrollBar Bar = scrollPane.getVerticalScrollBar();
		Bar.setUnitIncrement(60);

		for (Document doc : page.getDocList()) {
			String txt = doc.get(R.INDEX_NAMES.REAL_PATH);
			// rar文件判断
			Path p = Paths.get(txt);
			String name;
			if (p.getParent().toString().toLowerCase().endsWith(".zip")) {
				name = p.getParent().toString();
			} else
				name = new String(txt);
			JLabel label = cl(txt, g);
			JButton jb = cb(you, g, "打开文件");
			jb.setName(name);
			// 特殊转义字符要先处理
			String red = "";
			if (txt.getBytes().length > 97) {
				String txt2 = "";
				txt2 += txt.substring(0, 20) + "...";
				txt2 += txt.substring(txt.length()
						- p.getFileName().toString().length());
				red = txt2;
			} else
				red = txt;

			for (String r : page.getQueryStr().split(" ")) {
				red = MyHighlightUtil.highlight(red, r);
			}

			label.setText("<html>" + red + "</html>");
			panel.add(jb);
			JButton jb2 = cb(you + 100, g, "文件目录");
			jb2.setName("/" + name);
			panel.add(jb2);
			JButton jb3 = cb(you + 200, g, "复制文件");
			jb3.setName("<" + name);
			panel.add(jb3);
			panel.add(label);
			g += 35;
		}

		// 分页设置
		JButton pre = cb(you, (g + 50), "上一页");
		JButton next = cb((you + 200), (g + 50), "下一页");
		panel.add(pre);
		panel.add(next);

		// 设置panel的布局为任意null布局，这样下面的setBounds语句才能生效，并且label在这个面板的(125,75)位置，且大小为100x20px
		panel.setLayout(null);
		// 在frame中添加panel
		panel.setPreferredSize(new Dimension(scrollPane.getWidth() - 50,
				scrollPane.getHeight() * 2));
		this.add(scrollPane);
		panel.revalidate();
		// this.getContentPane().add(panel);

	}

	private JLabel cl(String txt, int gao) {

		JLabel label = new JLabel(txt);
		label.setBounds(25, gao, 1200, 40);
		Font F = new Font("宋体", Font.PLAIN, 15);
		label.setFont(F);
		return label;
	}

	private JButton cb(int you, int gao, String txt) {

		JButton jb = new JButton(txt);
		jb.setBounds(you, gao, 130, 30);
		jb.addActionListener(this);
		return jb;
	}

	public static void main(String[] args) {

		String s = "E:\\e盘专属.txt";
		System.out.println(s.toUpperCase());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JButton jb = (JButton) e.getSource();
		if (e.getActionCommand().equals("复制文件")) {
			new MyFilesUtil().copeFile(jb.getName().substring(1));
		} else if (e.getActionCommand().equals("文件目录")) {
			try {
				System.out.println(jb.getName().substring(1));
				Runtime.getRuntime().exec(
						"explorer /select, " + jb.getName().substring(1));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getActionCommand().equals("打开文件")) {
			try {
				System.out.println(jb.getName());
				Desktop.getDesktop().open(new File(jb.getName()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		// 如果是下一页，就传参并且
		else if (e.getActionCommand().equals("下一页")) {
			System.out.println("下一页");

			if (page.getDocList().size() > 0 && page.getDocList().size() == 20) {
				page.setCurrentPage(page.getCurrentPage() + 1);
				page = fs.search(page, true);
				if (page.getDocList().size() > 0)
					this.dispose();
			}
		} else if (e.getActionCommand().equals("上一页")) {
			System.out.println("上一页");
			// 只有大于第一页才有向前翻的价值
			if (page.getCurrentPage() > 1) {
				page.setCurrentPage(page.getCurrentPage() - 1);
				page = fs.search(page, true);
				this.dispose();
			}
		}
	}

}
