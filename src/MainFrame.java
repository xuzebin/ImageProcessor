import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;

import java.text.DecimalFormat;

public class MainFrame extends JFrame {
	
	private final int width = 455;
	private final int height = 450;
	private final String imgFilePath = "swfImages";
	//Flash播放
	private ImagePlayer imagePlayer = new ImagePlayer();
	private List<String> imageList = new ArrayList<String>();
	
	private ImageFileChooser imageFC;
	
	JTextField showVal = new JTextField(8);

	JSlider slider = new JSlider(20, 300);
	
	public MainFrame() {
		super();
		init();
		doShutDownWork();
	}
	//初始化界面
	public void init() {
		//组装界面
		createMenuBar();
		JPanel toolBar = createToolPanel();
		this.add(toolBar, BorderLayout.SOUTH);
		this.add(new JScrollPane(imagePlayer), BorderLayout.CENTER);
		new Thread(imagePlayer).start();//启动一个线程

		showVal.setText("FPS: 0.00 fps");
		// 设置界面
		this.setTitle("Flash Player");
		this.setLocation(500, 200);
		this.setPreferredSize(new Dimension(width, height));
		this.setVisible(true);
		this.pack();
	}
	//创建菜单栏
	public void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("文件");
		JMenuItem itemFile = new JMenuItem("打开");
		//加监听器
		itemFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				imageFC = new ImageFileChooser(".");
				int returnVal = imageFC.showOpenDialog(MainFrame.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = imageFC.getSelectedFile();
					String swfPath = file.getPath();
					File imgAddr = new File(imgFilePath);
					if (imgAddr.exists()) {
						if (imgAddr.isDirectory()) {
							File fs[] = imgAddr.listFiles();
							for (File f : fs) {
								f.delete();
							}
						}
						imgAddr.delete();
					}
					imgAddr.mkdir();
					SaveJPEGs jpegs = new SaveJPEGs();
					try {
						jpegs.swf2JPEGs(swfPath, imgFilePath);
					} catch(Exception es) {
						es.printStackTrace();
					}

					imagePlayer.changeFlash();
			   		File imgFile = new File(imgFilePath);
			   		if (imgFile.isDirectory()) {
			   			File[] files = imgFile.listFiles();

						//清空，支持二次加载图片
						imageList.clear();
						for (File f : files) {
							String path = f.getPath();
							imageList.add(path);
						}
						imagePlayer.setImageList(imageList);
			   		}
			   		imagePlayer.finishChange();
			   		DecimalFormat df = new DecimalFormat("#.00");

			   		showVal.setText("FPS: " + df.format(1000.0 / slider.getValue()) + " fps");
				}
			}
		});

		menu.add(itemFile);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
	//创建工具栏
	public JPanel createToolPanel() {		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		//toolBar中增加按钮	
		//写一个ImagePlayerListener
		JButton bnPlay = new JButton("播放");
		bnPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePlayer.play();
				DecimalFormat df = new DecimalFormat("#.00");
				showVal.setText("FPS: " + df.format(1000.0 / imagePlayer.getInterval()) + " fps");
			}
		});
		
		JButton bnPause = new JButton("暂停");
		bnPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePlayer.pause();
				showVal.setText("FPS: 0.00 fps");
			}
		});
	
		JButton bnSpeedUp = new JButton("加速");
		bnSpeedUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePlayer.speedUp();
				slider.setValue(imagePlayer.getInterval());
			}		
		});
		
		JButton bnSlowDown = new JButton("减速");
		bnSlowDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePlayer.slowDown();
				slider.setValue(imagePlayer.getInterval());
			}
		});
		//播放间隔 20ms - 300ms	
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(100);  
        slider.setMinorTickSpacing(20);
        slider.setValue(50);
        slider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				DecimalFormat df = new DecimalFormat("#.00");
				showVal.setText("FPS: " + df.format(1000.0 / source.getValue()) + " fps");
				imagePlayer.setInterval(source.getValue());
			}
        	
        });
        
        showVal.setEditable(false);
               
		toolBar.add(bnPlay);
		toolBar.add(bnPause);
		toolBar.add(bnSpeedUp);
		toolBar.add(bnSlowDown);
		toolBar.add(slider);
		toolBar.add(showVal);
	
		panel.add(toolBar);
		return panel;
	}

	//程序退出时，删除文件夹
	public void doShutDownWork() {
		Runtime run =  Runtime.getRuntime();
		run.addShutdownHook(new Thread() {
			public void run() {
				File imgAddr = new File(imgFilePath);
				if (imgAddr.exists()) {
					if (imgAddr.isDirectory()) {
						File fs[] = imgAddr.listFiles();
						for (File f : fs) {
							f.delete();
						}
					}
					imgAddr.delete();
				}
			}
		});
	}

}
