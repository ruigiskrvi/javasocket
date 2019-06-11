package homework3;

/*实现私聊功能，如何实现，在点击登录按钮时，
 * 
就要启动ServerSocket,监听是否有人要和自己进行私聊，生成一个私聊页面*/
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import homework1.User;
import test.MainW;
import test.TalkWin;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class cleentview extends JFrame {

	private JPanel contentPane;
	private JTextField txt_hostIP;
	private JTextField txt_port;
	private JTextField txt_name;
	private JTextField textField_3;
	private JTextArea textArea;
	private JList list;
	private String ip;
	private DefaultListModel listModel;  
	
	 private Socket socket;  
	 private Socket a=null;//本地监听
	 private PrintWriter writer;  
	 private BufferedReader reader;  
	 private MessageThread messageThread;// 负责接收消息的线程  
	  
	 private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户  
	 //用于记录在线用户
	 private ArrayList<User> userlist;
	 
	 User user;
	 String str="all";
	 private boolean isConnected = false;  
	/**
	 * 主方法Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					cleentview frame = new cleentview();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 *构造方法Create the frame.
	 */
	public cleentview() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 743, 470);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("IP地址");
		lblNewLabel.setBounds(10, 10, 54, 15);
		contentPane.add(lblNewLabel);
		
		txt_hostIP = new JTextField();
		txt_hostIP.setText("127.0.0.1");
		txt_hostIP.setBounds(74, 7, 99, 21);
		contentPane.add(txt_hostIP);
		txt_hostIP.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("端口");
		lblNewLabel_1.setBounds(183, 10, 54, 15);
		contentPane.add(lblNewLabel_1);
		
		txt_port = new JTextField();
		txt_port.setText("8000");
		txt_port.setBounds(217, 7, 66, 21);
		contentPane.add(txt_port);
		txt_port.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("昵称");
		lblNewLabel_2.setBounds(320, 10, 54, 15);
		contentPane.add(lblNewLabel_2);
		
		txt_name = new JTextField();
		txt_name.setBounds(384, 7, 66, 21);
		contentPane.add(txt_name);
		txt_name.setColumns(10);
		
		JButton btn_start = new JButton("登录");
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//所需的工作，
				//启动开启本地监听线程	localserver();。移除，移到一个开启私聊按钮上
				//可以的结果，是这个线程可能会一直填塞态，这样，就无法连接到服务器。6。8 0：
				
				//得到所需的信息，ip，端口，用户名
				String ip=txt_hostIP.getText();
				int port=Integer.parseInt(txt_port.getText());
				String username=txt_name.getText();
				//连接服务器
				boolean flag=connectServer(ip,port,username);
				 if (flag == false) {  
                     try {
						throw new Exception("与服务器连接失败!");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                 }  
				
				 JOptionPane.showMessageDialog(null, "成功连接!"+ "\r\n");
				 btn_start.setEnabled(false);
			}
		});
		btn_start.setBounds(492, 6, 93, 23);
		contentPane.add(btn_start);
		
		JButton btn_stop = new JButton("停止");
		btn_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {  
				
                if (!isConnected) {  
                
                    JOptionPane.showMessageDialog(null, "已处于断开状态，不要重复断开!",  
                            "错误", JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
                try {  
                    boolean flag = closeConnection();// 断开连接  
                    if (flag == false) {  
                        throw new Exception("断开连接发生异常！");  
                    }  
                    JOptionPane.showMessageDialog(null, "成功断开!"); 
                    
                } catch (Exception exc) {  
                    JOptionPane.showMessageDialog(null, exc.getMessage(),  
                            "错误", JOptionPane.ERROR_MESSAGE);  
                }  
                btn_stop.setEnabled(false);
                btn_start.setEnabled(true);
			}
		});
		btn_stop.setBounds(610, 6, 93, 23);
		contentPane.add(btn_stop);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(124, 45, 593, 305);
		contentPane.add(scrollPane_1);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane_1.setViewportView(textArea);
		
		textField_3 = new JTextField();
		textField_3.setBounds(124, 360, 593, 29);
		contentPane.add(textField_3);
		textField_3.setColumns(10);
		
		JButton btn_send = new JButton("发送");
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			send();
			}
		});
		btn_send.setBounds(624, 399, 93, 23);
		contentPane.add(btn_send);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 41, 103, 348);
		contentPane.add(scrollPane);
		
		/*
		 * list = new JList(); list.setEnabled(false);
		 */
		 listModel = new DefaultListModel();  
	     list = new JList(listModel);
	    
	  
	    
	     
	   
		scrollPane.setViewportView(list);
		
		JButton btnNewButton = new JButton("私聊");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			sendp2p();
			}
		});
		btnNewButton.setBounds(10, 399, 93, 23);
		contentPane.add(btnNewButton);
	}
	//执行发送
	public void send() {
		  
        if (!isConnected) {  
            JOptionPane.showMessageDialog(null, "还没有连接服务器，无法发送消息！", "错误",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        String message = textField_3.getText().trim();  
        if (message == null || message.equals("")) {  
            JOptionPane.showMessageDialog(null, "消息不能为空！", "错误",  
                    JOptionPane.ERROR_MESSAGE);  
            return;  
        }  
        sendMessage(txt_name.getText() + "@" + "ALL" + "@"+"all"+"@" + message);  
        textField_3.setText(null);  
    
		
		
	}
	//私聊发送
	
		public void sendp2p() {
			str=list.getSelectedValue().toString();
			System.out.println(str);
	        if (!isConnected) {  
	            JOptionPane.showMessageDialog(null, "还没有连接服务器，无法发送消息！", "错误",  
	                    JOptionPane.ERROR_MESSAGE);  
	            return;  
	        }  
	        String message = textField_3.getText().trim();  
	        if (message == null || message.equals("")) {  
	            JOptionPane.showMessageDialog(null, "消息不能为空！", "错误",  
	                    JOptionPane.ERROR_MESSAGE);  
	            return;  
	        }  
	        sendMessage(txt_name.getText()+ "@" + "P2P" + "@"+str+"@"+message);  
	        textField_3.setText(null);  
	    
			
			
		}
		//连接服务器
	public boolean connectServer(String ip,int port,String username) {
		try {
			socket=new Socket(ip,port);
			 writer = new PrintWriter(socket.getOutputStream());  
	         reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
	      // 发送客户端用户基本信息(用户名和ip地址)  
	            sendMessage(username + "@" + socket.getLocalAddress().toString());
	       // 开启接收消息的线程  
	            messageThread = new MessageThread(reader, textArea); 
	            messageThread.start();//开启线程
	            isConnected=true;//已经连接上了
	            return true;
		
		} catch (Exception e) {  
            textArea.append("与端口号为：" + port + "    IP地址为：" + ip + "   的服务器连接失败!" + "\r\n");  
            isConnected = false;// 未连接上  
            return false;  
        }  	
	}
	
	/**  
     * 
     *   
     * @param message  
     */  
	//发送消息
    public void sendMessage(String message) {  
        writer.println(message);  
        writer.flush();  
    } 
    
    
    //客户端主动关闭连接  
    
    @SuppressWarnings("deprecation")  
    public synchronized boolean closeConnection() {  
        try {  
            sendMessage("CLOSE");// 发送断开连接命令给服务器  
            messageThread.stop();// 停止接受消息线程  
            // 释放资源  
            if (reader != null) {  
                reader.close();  
            }  
            if (writer != null) {  
                writer.close();  
            }  
            if (socket != null) {  
                socket.close();  
            }  
            isConnected = false;  
            return true;  
        } catch (IOException e1) {  
            e1.printStackTrace();  
            isConnected = true;  
            return false;  
        }  
    }  
 
    // 不断接收消息的线程  
    class MessageThread extends Thread {
    	  private BufferedReader reader;  
          private JTextArea textArea;  
    
          // 接收消息线程的构造方法  
          public MessageThread(BufferedReader reader, JTextArea textArea) {  
              this.reader = reader;  
              this.textArea = textArea;  
          }  
          
       // 被动的关闭连接
              public synchronized void closeCon() throws Exception {  
            // 清空用户列表  
            listModel.removeAllElements();  
            // 被动的关闭连接释放资源  
            if (reader != null) {  
                reader.close();  
            }  
            if (writer != null) {  
                writer.close();  
            }  
            if (socket != null) {  
                socket.close();  
            }  
            isConnected = false;// 修改状态为断开  
        }  
              public void run() {
            	  
                  String message = "";  
                  while (true) {  
                      try {  
                          message = reader.readLine();  
                          StringTokenizer stringTokenizer = new StringTokenizer(  
                                  message, "/@");  
                          String command = stringTokenizer.nextToken();// 命令  
                          if (command.equals("CLOSE"))// 服务器已关闭命令  
                          {  
                              textArea.append("服务器已关闭!\r\n");  
                              closeCon();// 被动的关闭连接  
                              return;// 结束线程  
                          } else if (command.equals("ADD")) {// 有用户上线更新在线列表  
                              String username = "";  
                              String userIp = "";  
                              if ((username = stringTokenizer.nextToken()) != null  
                                      && (userIp = stringTokenizer.nextToken()) != null) {  
                                  //新建一个用户为
                            	  User user = new User(username, userIp);
                                  
                                  onLineUsers.put(username, user);  
                                  listModel.addElement(username);  
                              }  
                          } else if (command.equals("DELETE")) {// 有用户下线更新在线列表  
                              String username = stringTokenizer.nextToken();  
                              User user = (User) onLineUsers.get(username);  
                              onLineUsers.remove(user);  
                              listModel.removeElement(username);  
                          } 
                          
                          else if (command.equals("USERLIST")) {// 加载在线用户列表  
                              int size = Integer.parseInt(stringTokenizer.nextToken());  
                              String username = null;  
                              String userIp = null;  
                              for (int i = 0; i < size; i++) {  
                                  username = stringTokenizer.nextToken();  
                                  userIp = stringTokenizer.nextToken();  
                                  User user = new User(username, userIp);  
                                  onLineUsers.put(username, user);  
                                  listModel.addElement(username);  
                              }  
                          }
                          else if (command.equals("P2P")) {
                        	  message=stringTokenizer.nextToken();
                        	 
                        	  textArea.append(message +"个人消息");
                          }else {// 普通消息  
                              textArea.append(message + "\r\n");  
                          }  
                      } catch (IOException e) {  
                          e.printStackTrace();  
                      } catch (Exception e) {  
                          e.printStackTrace();  
                      }  
                  }  
              }
              }
}
