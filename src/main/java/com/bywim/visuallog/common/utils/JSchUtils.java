package com.bywim.visuallog.common.utils;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * JSch方法类
 *
 * @Author shiyuebin
 * @Date 2020/4/29 14:32
 */
public class JSchUtils {
    private static final Logger LOGGER = LoggerFactory
            .getLogger("JSchUtils");
    private static JSch jsch;
    private static Session session;
    private static Vector<String> stdout;


    /**
     * 连接到指定的IP
     *
     * @throws JSchException
     */
    public static void connect(String user, String passwd, String host, int port) throws JSchException {
        jsch = new JSch();// 创建JSch对象
        session = jsch.getSession(user, host, port);// 根据用户名、主机ip、端口号获取一个Session对象
        session.setPassword(passwd);// 设置密码
        //网络时间,日志
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);// 为Session对象设置properties
        session.setTimeout(1500);// 设置超时
        session.connect();// 通过Session建立连接
    }

    /**
     * 连接到指定的IP
     *
     * @throws JSchException
     */
    public static void connectNoPwd(String user,String pubKeyPath,String host, int port) throws JSchException {
        jsch = new JSch();// 创建JSch对象
        jsch.addIdentity(pubKeyPath);
        session = jsch.getSession(user, host, port);// 根据用户名、主机ip、端口号获取一个Session对象
        //网络时间,日志
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);// 为Session对象设置properties
        session.setTimeout(1500);// 设置超时
        session.connect();// 通过Session建立连接
    }

    /**
     * 关闭连接
     */
    public static void close() {
        session.disconnect();
    }

    public static boolean execSh(String command) throws JSchException {
        boolean isSuccess=false;
        String result="";
        BufferedReader reader = null;
        Channel channel = null;
        try {
            if (command != null) {
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                // ((ChannelExec) channel).setErrStream(System.err);
                channel.connect();
                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                byte[] tmp=new byte[1024];
                while(true) {
                    while(in.available()>0){
                        int i=in.read(tmp, 0, 1024);
                        if(i<0)break;
                        LOGGER.info(new String(tmp, 0, i));
                    }
                    if(channel.isClosed()){
                        LOGGER.info("exit-status: "+channel.getExitStatus());
                        if(channel.getExitStatus()==0){
                            isSuccess= true;
                        }
                        break;
                    }
                    try{Thread.sleep(1000);}catch(Exception ee){}
                }
            }
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel.disconnect();
        }
        return  isSuccess;
    }


    public static int execute(final String command) {
        int returnCode = 0;
        JSch jsch = new JSch();

        try {
            // Create and connect channel.
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader(channel
                    .getInputStream()));

            channel.connect();
            LOGGER.info("The remote command is: " + command);

            // Get the output of remote command.
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
            }
            input.close();

            // Get the return code only after the channel is closed.
            if (channel.isClosed()) {
                returnCode = channel.getExitStatus();
                LOGGER.info("exit-status: "+returnCode);
            }

            // Disconnect the channel and session.
            channel.disconnect();
            //session.disconnect();
        } catch (JSchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnCode;
    }

    /**
     * 执行相关的命令
     *
     * @throws JSchException
     */
    public static String execCmd(String command) throws JSchException {
        String result="";
        BufferedReader reader = null;
        Channel channel = null;
        try {
            if (command != null) {
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                // ((ChannelExec) channel).setErrStream(System.err);
                channel.connect();
                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String buf = null;
                while ((buf = reader.readLine()) != null) {
                    result+= new String(buf.getBytes("UTF-8"),"UTF-8")+"/r/n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel.disconnect();
        }
        LOGGER.info("result: "+result);
        return  result;
    }

    /**
     * 上传文件
     *
     * @param directory  上传的目录
     * @param uploadFile 要上传的文件
     * @throws JSchException
     * @throws SftpException
     * @throws FileNotFoundException
     */
    public static void upload(String directory, String uploadFile) throws JSchException, FileNotFoundException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        channelSftp.cd(directory);
        File file = new File(uploadFile);
        channelSftp.put(new FileInputStream(file), file.getName());
        System.out.println("Upload Success!");
    }

    /**
     * 下载文件
     *
     * @param src
     * @param dst
     * @throws JSchException
     * @throws SftpException
     */
    public static void download(String src, String dst) throws JSchException, SftpException {
        // src linux服务器文件地址，dst 本地存放地址
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        channelSftp.get(src, dst);
        channelSftp.quit();
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     * @throws SftpException
     * @throws JSchException
     */
    public void delete(String directory, String deleteFile) throws SftpException, JSchException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.cd(directory);
        channelSftp.rm(deleteFile);
    }

    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     * @return
     * @throws SftpException
     * @throws JSchException
     */
    @SuppressWarnings("rawtypes")
    public static Vector listFiles(String directory) throws JSchException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        return channelSftp.ls(directory);
    }

    public static void main(String[] args) {
        try {
            // 1.连接到指定的服务器
            connect("root", "ahjcjy@2017", "47.96.27.42", 22);

            // 2.执行相关的命令
           // execCmd("echo '160622150549943666' ");
            //String cmd = "cd /home/lxm/" + ";" + "rm -rf 新建文本文档 (3).txt";
            //String cmd = "cd /" + ";" + "ls -al |grep home";
            try {
                //execCmd(cmd);
                String cmd = "tail -500 /usr/tomcat6/tomcat6/logs/catalina.out";
                String result= execCmd(cmd);// 多条命令之间以;分隔
                System.out.println(result);

            } catch (Exception e) {
                e.getMessage();
            }
//            try {
//                upload("/home/lxm/","D:\\test\\新建文本文档 (3).txt");
//            } catch (FileNotFoundException e) {
//
//            } catch (SftpException e) {
//
//            }

            //查看目录
            //Vector vector = listFiles("/usr");
            // 3.下载文件
            //download("/mnt/1.txt", "D:\\xfmovie");
            //System.out.println("11111");
            // 4.关闭连接
            close();
        } catch (JSchException e) {
            e.printStackTrace();
        } /*catch (SftpException e) {
            e.printStackTrace();
        }*/
    }
}