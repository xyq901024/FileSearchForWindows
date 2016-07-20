package com.xyq.fs.login;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.lucene.document.Document;

import com.xyq.fs.base.BaseBootUp;
import com.xyq.fs.base.MyDirectory;
import com.xyq.fs.entity.Page;
import com.xyq.fs.resource.R;
import com.xyq.fs.service.FSService;
import com.xyq.fs.thread.SearchThread;
import com.xyq.fs.util.Load;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import static javafx.geometry.HPos.RIGHT;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SearchWindow extends Application {

	private FSService fs = new FSService();
	public static final Text actiontarget = new Text();

	public static final Button btn5 = new Button("文件监控");
	// 索引进度条
	public static final ProgressBar pb = new ProgressBar();

	static final Component cp = new Component() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	};
	/**
	 * 正在索引中的提示
	 */
	public static final Text indexingTxt = new Text();

	@Override
	public void start(Stage primaryStage) {

		/**
		 * 窗口关闭事件
		 */
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {

				fs.closeIndexSystem();
				fs.closeFileMonitor();
				MyDirectory.commit();
				MyDirectory.closeAll();
				if (FSService.timer != null)
					FSService.timer.cancel();
				if (BaseBootUp.SERVER != null)
					try {
						BaseBootUp.SERVER.close();
					} catch (Exception e) {
						System.exit(0);
					}
			}
		});

		primaryStage.setTitle("文件搜索工具(现代版)1.3.5");
		final GridPane grid = new GridPane();

		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Text scenetitle = new Text("Welcome");
		scenetitle.setId("welcome-text");
		grid.add(scenetitle, 0, 0, 2, 1);

		Label userName = new Label("关键词:");
		grid.add(userName, 0, 1);

		// 搜索框
		final TextField userTextField = new TextField();
		userTextField.setPrefWidth(300);
		userTextField.setPrefHeight(40);
		userTextField.setId("tx1");
		grid.add(userTextField, 1, 1);

		Button btn = new Button("搜索");
		btn.setPrefSize(70, 40);
		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);

		Button btn2 = new Button("清除");
		btn2.setPrefSize(70, 40);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn2);

		Button btn3 = new Button("搜索设置");
		btn3.setPrefSize(130, 40);
		grid.add(btn3, 1, 5);

		HBox hbBtn2 = new HBox(10);
		hbBtn2.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn2.getChildren().add(btn3);

		Button btn4 = new Button("运行/停止索引");
		btn4.setPrefSize(130, 40);
		grid.add(btn4, 1, 5);
		hbBtn2.getChildren().add(btn4);

		btn5.setPrefSize(130, 40);
		grid.add(btn5, 1, 5);
		hbBtn2.getChildren().add(btn5);

		grid.add(hbBtn, 4, 1);
		grid.add(hbBtn2, 1, 5);

		/**
		 * 关键词错误的提示
		 */

		grid.add(actiontarget, 2, 7);
		GridPane.setColumnSpan(actiontarget,4);
		GridPane.setHalignment(actiontarget, RIGHT);
		actiontarget.setId("actiontarget");

		indexingTxt.setId("actiontarget");

		final Stage fileTypeStage = new Stage();

		final ConfigWindows ftcb = new ConfigWindows();
		ftcb.start(fileTypeStage);
		fileTypeStage.hide();

		// 索引文本显示
		grid.add(indexingTxt, 0, 6);

		pb.setProgress(-1.0f);
		pb.setVisible(false);
		grid.add(pb, 1, 6);
		/**
		 * 文件监控按钮
		 */
		btn5.setOnAction(new EventHandler<ActionEvent>(

		) {
			@Override
			public void handle(ActionEvent arg0) {

				String txt = btn5.getText();
				if ("文件监控".equals(txt)) {
					if (R.FILE_MONITOR.MONITOR_DIRS.size() == 0) {
						JOptionPane.showMessageDialog(null, "请先设置监控目录!!!");
						return;
					}
					if (R.TOKENS.MONITOR_NUM.get() == 1) {
						JOptionPane.showMessageDialog(null,
								"请在索引结束后开启文件监控功能!!!");
						return;
					}
					fs.openFileMonitor();
					txt = "停止监控";
				} else {
					fs.closeFileMonitor();
					txt = "文件监控";
				}
				btn5.setText(txt);
			}
		});

		/**
		 * 索引按钮事件
		 */
		btn4.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				if (R.TOKENS.INDEX_STATU.get() == 1) {
					int value = JOptionPane.showConfirmDialog(cp, "是否需要停止索引?");
					if (value == JOptionPane.OK_OPTION) {
						fs.closeIndexSystem();
						actiontarget.setText("");
						pb.setVisible(false);
						indexingTxt.setText("");
					}
				} else {
					indexingTxt.setText("索引中...");
					pb.setVisible(true);
					fs.openIndexSystem();
				}
			}
		});

		/**
		 * 选择文件类型事件
		 */
		btn3.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				if (!fileTypeStage.isShowing()) {
					fileTypeStage.show();
				} else
					fileTypeStage.hide();
			}
		});

		/**
		 * 文本框点击事件，和搜索按钮一致
		 */
		userTextField.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				search(actiontarget, userTextField);
			}
		});

		/**
		 * 清除按钮时间
		 */
		btn2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				userTextField.setText("");
				if (!"系统正在索引中...".equals(actiontarget.getText()))
					actiontarget.setText("");
			}
		});

		/**
		 * 搜索按钮事件
		 */
		btn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {

				search(actiontarget, userTextField);
			}
		});

		Scene scene = new Scene(grid, 680, 475);
		primaryStage.setScene(scene);
		scene.getStylesheets().add(
				SearchWindow.class.getResource("Login.css").toExternalForm());
		primaryStage.show();
		fs.indexSelfCheck();
		Load.init();
	}

	/**
	 * 点击搜索按钮和搜索框的搜索时间
	 * 
	 * @param actiontarget
	 * @param userTextField
	 */
	private void search(Text actiontarget, TextField userTextField) {

		if (R.TOKENS.INDEX_STATU.get() == 1) {
		} else
			actiontarget.setText("");
		String queryStr = userTextField.getText().replaceAll("\\s+", " ")
				.trim();
		if ("".equals(queryStr) || queryStr == null)
			actiontarget.setText("关键词不能为空!!!");
		else {
			Page<Document> page = new Page<>();
			page.setCurrentPage(1);
			page.setQueryStr(queryStr);
			SearchThread st = new SearchThread(page);
			st.setPriority(10);
			st.start();
		}
		System.out.println(queryStr);
	}

	public static void main(String[] args) {

		if (BaseBootUp.bindPort())
			launch(args);
		else {
			JOptionPane.showMessageDialog(null, "搜索程序已启动!!!");
			System.exit(0);
		}
	}
}