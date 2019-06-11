package homework3;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import homework1.User;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class serverchat extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextArea textArea;
	//用户列表
	private JList list ;
    private DefaultListModel listModel;  
	 
    private ServerSocket serverSocket;  
    private ServerThread serverThread;  
   
    private ArrayList<ClientThread> clients;   
	/**
	 * Launch the application.
	 *主方法，建立一个服务页面*/
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					serverchat frame = new serverchat();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 *
	 *构造方法*/
	public serverchat() {
		setTitle("服务器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 434);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("端口");
		lblNewLabel.setBounds(10, 10, 54, 15);
		contentPane.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setText("8000");
		textField.setBounds(37, 7, 66, 21);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 35, 414, 193);
		contentPane.add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 265, 414, 130);
		contentPane.add(scrollPane_1);
		
		
		listModel = new DefaultListModel(); 
		list=new JList(listModel); 
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			
			}
		});
	
		scrollPane_1.setViewportView(list);
		JButton btnNewButton = new JButton("启动");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				btnNewButton.setEnabled(false);
				int port=Integer.parseInt(textField.getText());
				//启动服务器
				try {
					serverStart(port);
					JOptionPane.showMessageDialog(null, "服务器成功启动!"+ "\r\n");
					textArea.append("服务器成功启动!"+ "\r\n");
				} catch (BindException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		btnNewButton.setBounds(208, 6, 93, 23);
		contentPane.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("停止");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			//关闭服务器
					closeServer();
			}
		});
		btnNewButton_1.setBounds(331, 6, 93, 23);
		contentPane.add(btnNewButton_1);
		
		JLabel lblNewLabel_1 = new JLabel("用户列表");
		lblNewLabel_1.setBounds(10, 240, 54, 15);
		contentPane.add(lblNewLabel_1);
		
		
	}
	
	//启动服务器
	 public void serverStart(int port) throws java.net.BindException {
		  
	        try {  
	            clients = new ArrayList<ClientThread>();  
	            serverSocket = new ServerSocket(port);  
	            serverThread = new ServerThread(serverSocket);  
	            serverThread.start();  
	           
	        } catch (BindException e) {  
	            
	            throw new BindException("端口号已被占用，请换一个！");  
	        } catch (Exception e1) {  
	            e1.printStackTrace();  
	            throw new BindException("启动服务器异常！");  
	        }  
	    
		 
		 
	 }
	 
	 
	 
	 
	  // 关闭服务器  
    public void closeServer() {
    	  
        try {  
            if (serverThread != null)  
                serverThread.stop();// 停止服务器线程  
  
            for (int i = clients.size() - 1; i >= 0; i--) {  
                // 给所有在线用户发送关闭命令  
                clients.get(i).getWriter().println("CLOSE");  
                clients.get(i).getWriter().flush();  
                // 释放资源  
                clients.get(i).stop();// 停止此条为客户端服务的线程  
                clients.get(i).reader.close();  
                clients.get(i).writer.close();  
                clients.get(i).socket.close();  
                clients.remove(i);  
            }  
            if (serverSocket != null) {  
                serverSocket.close();// 关闭服务器端连接  
            }  
            listModel.removeAllElements();// 清空用户列表  
             
        } catch (IOException e) {  
            e.printStackTrace();     
        }  
    
    } 
    
    
    
    
    // 服务器线程  
    class ServerThread extends Thread { 
    	 private ServerSocket serverSocket; 
    	 // 服务器线程的构造方法  
         public ServerThread(ServerSocket serverSocket) {  
             this.serverSocket = serverSocket;  
         }  
         public void run() {
        	 //不停的等待客户端的连接
        	 while(true) {
        		 try {
        			 Socket socket = serverSocket.accept();  
        			 ClientThread client = new ClientThread(socket);  
        			 client.start();
        			 clients.add(client);
        			 //
        			 listModel.addElement(client.getUser().getName());// 更新在线列表
        			 textArea.append(client.getUser().getName() + client.getUser().getIp() + "上线!\r\n"); 
        		 }catch(IOException e) {  
                     e.printStackTrace();  
                 }  
        	 }
        	 
        	 
         }
    }
    
    
    
    //为一个客户端服务的线程
    class ClientThread extends Thread {  
    	 private Socket socket;  
         private BufferedReader reader;  
         private PrintWriter writer;  
         private User user;  
   
         public BufferedReader getReader() {  
             return reader;  
         }  
   
         public PrintWriter getWriter() {  
             return writer;  
         }  
   
         public User getUser() {  
             return user;  
         }  
    	// 客户端线程的构造方法  
        public ClientThread(Socket socket) {  
            try {  
                this.socket = socket;  
                reader = new BufferedReader(new InputStreamReader(socket  
                        .getInputStream()));  
                writer = new PrintWriter(socket.getOutputStream());  
                // 接收客户端的基本用户信息  
                String inf = reader.readLine();  
                StringTokenizer st = new StringTokenizer(inf, "@");  
                user = new User(st.nextToken(), st.nextToken());  
                // 反馈连接成功信息  
                writer.println(user.getName() + user.getIp() + "与服务器连接成功!");  
                writer.flush();  
                // 反馈当前在线用户信息  
                if (clients.size() > 0) {  
                    String temp = "";  
                    for (int i = clients.size() - 1; i >= 0; i--) {  
                        temp += (clients.get(i).getUser().getName() + "/" + clients  
                                .get(i).getUser().getIp())  
                                + "@";  
                    }  
                    writer.println("USERLIST@" + clients.size() + "@" + temp);  
                    writer.flush();  
                }  
                // 向所有在线用户发送该用户上线命令  
                for (int i = clients.size() - 1; i >= 0; i--) {  
                    clients.get(i).getWriter().println(  
                            "ADD@" + user.getName() + user.getIp());  
                    clients.get(i).getWriter().flush();  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        @SuppressWarnings("deprecation")  
        public void run() {// 不断接收客户端的消息，进行处理。  
            String message = null;  
            while (true) {  
                try {  
                    message = reader.readLine();// 接收客户端消息  
                    if (message.equals("CLOSE"))// 下线命令  
                    {  
                        textArea.append(this.getUser().getName()  
                                + this.getUser().getIp() + "下线!\r\n");  
                        // 断开连接释放资源  
                        reader.close();  
                        writer.close();  
                        socket.close();  
  
                        // 向所有在线用户发送该用户的下线命令  
                        for (int i = clients.size() - 1; i >= 0; i--) {  
                            clients.get(i).getWriter().println(  
                                    "DELETE@" + user.getName());  
                            clients.get(i).getWriter().flush();  
                        }  
  
                        listModel.removeElement(user.getName());// 更新在线列表  
  
                        // 删除此条客户端服务线程  
                        for (int i = clients.size() - 1; i >= 0; i--) {  
                            if (clients.get(i).getUser() == user) {  
                                ClientThread temp = clients.get(i);  
                                clients.remove(i);// 删除此用户的服务线程  
                                temp.stop();// 停止这条服务线程  
                                return;  
                            }  
                        }  
                    } else {  
                        dispatcherMessage(message);// 转发消息 
                }
                    } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    
   
        // 转发消息  
        public void dispatcherMessage(String message) {  
            StringTokenizer stringTokenizer = new StringTokenizer(message, "@");  
            String source = stringTokenizer.nextToken();  
            String owner = stringTokenizer.nextToken(); 
            String name=stringTokenizer.nextToken();
            String content = stringTokenizer.nextToken();  
            message = source + "说：" + content;  //用户名+“说”+信息
            
             
            if (owner.equals("ALL")) {// 群发  
                for (int i = clients.size() - 1; i >= 0; i--) {  
                    clients.get(i).getWriter().println(message);  //显示在客户端
                    clients.get(i).getWriter().flush();  
                }  
                	//textArea.append(message +"" +"\r\n");
            }  
            //私聊
            if (owner.equals("P2P")) {// 私聊没有启用
              //查看用户是否存在
            	for(int i=clients.size()-1;i>0;i--) {
            		if(name.equals(clients.get(i).getUser().getName())) {
            			clients.get(i).getWriter().println("P2P@"+message);  
                        clients.get(i).getWriter().flush(); 
                     
            		}
            		
            	}
            }  
            
        }  
    }	
  }
    

