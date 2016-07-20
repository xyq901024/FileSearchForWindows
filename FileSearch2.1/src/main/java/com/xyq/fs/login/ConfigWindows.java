package com.xyq.fs.login;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xyq.fs.resource.R;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 文件类型复选框
 * 
 * @author xyq
 * 
 */
public class ConfigWindows extends Application {

	private Map<String, Set<CheckBox>> map = new HashMap<>();

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("搜索设置");
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.BASELINE_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.getColumnConstraints().add(new ColumnConstraints(100));
		grid.setPadding(new Insets(25, 25, 25, 25));

		int i = 1;

		for (String rootName : R.TYPES.INDEX_TYPES_MAP.keySet()) {

			final CheckBox cb = new CheckBox(rootName);
			/**
			 * 类型选中事件
			 */
			cb.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent e) {

					Set<CheckBox> set = map.get(cb.getText());
					if (cb.isSelected()) {
						for (CheckBox c : set) {
							c.setSelected(true);
						}
					} else {
						for (CheckBox c : set) {
							c.setSelected(false);
						}
					}
				}
			});

			Text txt1 = new Text("设置搜索文件的类型(不选默认全类型)");
			grid.add(txt1, 0, 0);

			grid.add(cb, 0, i);
			Set<String> list = R.TYPES.INDEX_TYPES_MAP.get(rootName);
			List<CheckBox> clist = new ArrayList<>();
			int y = 0;
			Set<CheckBox> set = new HashSet<>();
			boolean l = false;
			for (String type : list) {

				final CheckBox cb1 = new CheckBox(type);
				cb1.setPrefSize(65, 55);
				set.add(cb1);
				// 每一个文件类型加上事件
				cb1.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {

						if (cb1.isSelected())
							R.SEARCHS.QUERIES_FILE_TYPES.add(cb1.getText());
						else
							R.SEARCHS.QUERIES_FILE_TYPES.remove(cb1.getText());
					}
				});
				//类型太多的换行处理
				if (y > 10 && !l) {
					i++;
					y = 0;
					l = true;
				}
				grid.add(cb1, (y + 1), i);
				clist.add(cb1);
				y++;
			}
			i++;
			map.put(rootName, set);

		}

		Text txt2 = new Text("设置搜索范围");
		grid.add(txt2, 0, ++i);
		i++;
		Iterable<Path> roots = FileSystems.getDefault().getRootDirectories();
		int z = 1;
		CheckBox desk = createSearchCheckBox("桌面", false);
		grid.add(desk, z++, i);
		for (Path root : roots) {
			CheckBox cb1 = createSearchCheckBox(root.toString(), false);
			grid.add(cb1, z++, i);
		}

		Text txt3 = new Text("设置索引范围");
		z = 1;
		grid.add(txt3, 0, ++i);
		CheckBox desk2 = createIndexCheckBox("桌面", false);
		grid.add(desk2, z++, ++i);

		for (Path root : roots) {
			CheckBox cb1 = createIndexCheckBox(root.toString(), false);
			grid.add(cb1, z++, i);
		}

		Text txt4 = new Text("设置文件监控范围");
		z = 1;
		grid.add(txt4, 0, ++i);
		CheckBox desk3 = createFileMonitorCheckBox("桌面", false);
		grid.add(desk3, z++, ++i);

		for (Path root : roots) {
			CheckBox cb1 = createFileMonitorCheckBox(root.toString(), false);
			grid.add(cb1, z++, i);
		}

		Scene scene = new Scene(grid, 1030, 675);
		primaryStage.setScene(scene);
		scene.getStylesheets().add(
				SearchWindow.class.getResource("Login.css").toExternalForm());
		//primaryStage.show();

	}

	/**
	 * 创建索引目录的事件
	 * 
	 * @param title
	 * @param select
	 * @return
	 */
	private CheckBox createIndexCheckBox(String title, boolean select) {

		final CheckBox cb = new CheckBox(title);
		if (select)
			cb.setSelected(true);
		cb.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				String deskPath = System.getProperty("user.home") + "\\Desktop";
				Path path;
				if ("C:\\".equals(cb.getText())) {
					R.SEARCHS.INDEX_PARENT_PATH.remove(Paths.get(deskPath));
				}
				if ("桌面".equals(cb.getText())) {
					path = Paths.get(deskPath);
					if (R.SEARCHS.INDEX_PARENT_PATH.contains(Paths.get("C:\\"))) {
						return;
					}
				} else
					path = Paths.get(cb.getText());

				if (cb.isSelected())
					R.SEARCHS.INDEX_PARENT_PATH.add(path);
				else
					R.SEARCHS.INDEX_PARENT_PATH.remove(path);
			}
		});

		return cb;
	}

	/**
	 * 创建查询目录的事件
	 * 
	 * @param title
	 * @param select
	 * @return
	 */
	private CheckBox createSearchCheckBox(String title, boolean select) {

		final CheckBox cb = new CheckBox(title);
		if (select)
			cb.setSelected(true);
		cb.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				String txt = cb.getText();
				String deskPath = System.getProperty("user.home") + "\\Desktop";
				if ("桌面".equals(cb.getText())) {
					txt = deskPath;
					if (R.SEARCHS.QUERIES_PARENT_PATH.contains("C:\\"))
						return;
				} else if ("C:\\".equals(cb.getText())) {
					R.SEARCHS.QUERIES_PARENT_PATH.remove(deskPath);
				}
				if (cb.isSelected())
					R.SEARCHS.QUERIES_PARENT_PATH.add(txt);
				else
					R.SEARCHS.QUERIES_PARENT_PATH.remove(txt);
			}
		});

		return cb;
	}

	/**
	 * 创建文件监控目录的事件
	 * 
	 * @param title
	 * @param select
	 * @return
	 */
	private CheckBox createFileMonitorCheckBox(String title, boolean select) {

		final CheckBox cb = new CheckBox(title);
		if (select)
			cb.setSelected(true);
		cb.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				String txt = cb.getText();
				String deskPath = System.getProperty("user.home") + "\\Desktop";
				if ("桌面".equals(cb.getText())) {
					txt = deskPath;
					if (R.FILE_MONITOR.MONITOR_DIRS.contains("C:\\"))
						return;
				} else if ("C:\\".equals(cb.getText())) {
					R.FILE_MONITOR.MONITOR_DIRS.remove(deskPath);
				}
				if (cb.isSelected())
					R.FILE_MONITOR.MONITOR_DIRS.add(txt);
				else
					R.FILE_MONITOR.MONITOR_DIRS.remove(txt);
			}
		});

		return cb;
	}

	public static void main(String[] args) {

		launch(args);
	}

}