package com.ainory.dev.utils.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by ainory on 2016. 7. 19..
 */
public class SshUtil {
    private static final Logger logger = LoggerFactory.getLogger(SshUtil.class);

    private static final String ALIVE = "Alive";
    private static final String SUDO_COMMAND_PREFIX = "sudo -S -p '' ";

    /**
     *
     * @param host
     * @param user
     * @param pass
     * @param port
     * @param timeout
     * @return
     */
    public static String getNodeAliveStr(String host, String user, String pass, int port, int timeout){

        Session session = null;

        try{
            session = getSshSession( host,  user,  pass,  port,  timeout);
            session.connect();

            session.isConnected();

            return ALIVE;
        }catch (Exception e){
//            logger.error(ExceptionUtils.getStackTrace(e));
            return ExceptionUtils.getMessage(e);
        }finally {
            if (session != null){
                session.disconnect();
            }
        }

    }

    /**
     *
     * @param host
     * @param user
     * @param pass
     * @param port
     * @param timeout
     * @return
     */
    public static boolean isNodeAlive(String host, String user, String pass, int port, int timeout){

        Session session = null;
        boolean isAlive = false;

        try{
            session = getSshSession( host,  user,  pass,  port,  timeout);
            session.connect();

            isAlive = session.isConnected();

            return isAlive;
        }catch (Exception e){
//            logger.error(ExceptionUtils.getStackTrace(e));
            return isAlive;
        }finally {
            if (session != null){
                session.disconnect();
            }
        }
    }

    /**
     *
     * @param host
     * @param user
     * @param pass
     * @param port
     * @param timeout - millisecond
     * @return SSH Session
     */
    public static Session getSshSession(String host, String user, String pass, int port, int timeout){

        JSch jSch = new JSch();
        Session session = null;
        try {
            session = jSch.getSession(user,host,port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pass);
            session.setTimeout(timeout*1000);

            return session;
        } catch (Exception e) {
            logger.error("**** " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     *
     *
     * @param host
     * @param user
     * @param port
     * @param timeout - millisecond
     * @return SSH Session
     */
    public static Session getSshSession(String host, String user, int port, int timeout){

        JSch jSch = new JSch();
        Session session = null;
        try {
            session = jSch.getSession(user,host,port);
            session.setConfig("PreferredAuthentications", "publickey");
            jSch.setKnownHosts("~/.ssh/known_hosts");
            jSch.addIdentity("~/.ssh/id_rsa");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(timeout*1000);

            return session;
        } catch (Exception e) {
            logger.error("**** " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }


    /**
     * Execution Command
     *
     * @param session
     * @param command
     * @return
     */
    public static String execCommand(Session session, String command){

        StringBuffer resultMsg = new StringBuffer();

        try{

            if(!session.isConnected()){
                session.connect();
            }

            ChannelExec m_channelExec = (ChannelExec) session.openChannel("exec");

            m_channelExec.setCommand(command);

            InputStream m_in = m_channelExec.getInputStream();
            m_channelExec.setPty(true);
            m_channelExec.connect();

            InputStreamReader inputReader = new InputStreamReader(m_in);
            BufferedReader bufferedReader = new BufferedReader(inputReader);
            String line = null;

            while((line = bufferedReader.readLine()) != null){
                if(StringUtils.isNotEmpty(line)){
                    resultMsg.append(line).append(StringUtils.LF);
                }
            }
            bufferedReader.close();
            inputReader.close();

            m_channelExec.disconnect();

            return resultMsg.toString();
        }catch (Exception e){
            logger.error(ExceptionUtils.getRootCauseMessage(e));
//            return ExceptionUtils.getRootCauseMessage(e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Execute Command(sudo use)
     *
     * @param session
     * @param command
     * @param sudoPasswd
     * @return
     */
    public static String execCommand(Session session, String command, String sudoPasswd){

        StringBuffer resultMsg = new StringBuffer();

        try{

            if(!session.isConnected()){
                session.connect();
            }

            ChannelExec m_channelExec = (ChannelExec) session.openChannel("exec");

            command = SUDO_COMMAND_PREFIX+command;

            logger.debug(command);

            m_channelExec.setCommand(command);

            InputStream m_in = m_channelExec.getInputStream();
            OutputStream m_out = m_channelExec.getOutputStream();

            m_channelExec.setPty(true);
            m_channelExec.connect();

            m_out.write((sudoPasswd+StringUtils.LF).getBytes());
            m_out.flush();

            InputStreamReader inputReader = new InputStreamReader(m_in);
            BufferedReader bufferedReader = new BufferedReader(inputReader);
            String line;

            while((line = bufferedReader.readLine()) != null){
                if(StringUtils.contains(line, sudoPasswd)){
                    continue;
                }
                if(StringUtils.isNotEmpty(line)){
                    resultMsg.append(line).append(StringUtils.LF);
                }
            }

            bufferedReader.close();
            inputReader.close();

            m_channelExec.disconnect();

            return resultMsg.toString();

        }catch (Exception e){
            logger.error(ExceptionUtils.getRootCauseMessage(e));
            return ExceptionUtils.getRootCauseMessage(e);
        }
    }

    public static void main(String[] args) {

        String host = "192.168.10.22";
        String user = "user";
        String pass = "userpass";
        int port = 22;
        int timeout = 3000;

        logger.debug(getNodeAliveStr(host,user,pass, port, timeout));
        logger.debug(String.valueOf(isNodeAlive(host,user,pass, port, timeout)));

    }
}
