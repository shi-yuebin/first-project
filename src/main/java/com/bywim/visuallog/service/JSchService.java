package com.bywim.visuallog.service;

import com.bywim.visuallog.common.config.ConfigBeanProp;
import com.bywim.visuallog.common.utils.JSchUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

/**
 * 日志读取业务类
 *
 * @Author shiyuebin
 * @Date 2020/4/29 14:32
 */
@Service
public class JSchService {

    /**
     * 配置信息
     */
    @Autowired
    private ConfigBeanProp pro;

    /**
     *
     * @param lineNum 查询行数
     * @param logFile 目标文件
     * @return
     */
    public String readLog(String logFile, Integer lineNum){

        // 不知定则默认100行
        if (lineNum == null) {
            lineNum = 100;
        }

        String result = "";
        try {

            // 创建连接
            String host = pro.getHost();
            String user = pro.getUser();
            String password = pro.getPassword();
            int port = pro.getPort();
            JSchUtils.connect(user,password,host,port);

            // 执行cmd命令
            String  command = "tail -n" + " " + lineNum + " " + logFile;
            result = JSchUtils.execCmd(command);
            result = result.replace("/r/n", "</br>");
        } catch (JSchException e) {
            e.printStackTrace();
        } finally{

            // 关闭连接
            JSchUtils.close();
        }

        return result;
    }

    /**
     *
     * @param directory 查询行数
     * @param uploadFile 目标文件
     * @return
     */
    public boolean uploadFile(String directory, String uploadFile){

        try {

            // 创建连接
            String host = pro.getHost();
            String user = pro.getUser();
            String password = pro.getPassword();
            int port = pro.getPort();
            JSchUtils.connect(user,password,host,port);

            // 上传文件
            JSchUtils.upload("/test", "D:\\test\\test.txt");
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } finally{

            // 关闭连接
            JSchUtils.close();
        }

        return true;
    }

}
