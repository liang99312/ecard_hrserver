/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jhrcore.server;

import hrlancher.IDeplyment;
import hrlancher.JarInfomation;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author wangzhenhua
 */
public class IDeplymentImpl extends UnicastRemoteObject implements IDeplyment {

    private Logger log = Logger.getLogger(IDeplymentImpl.class.getName());

    public IDeplymentImpl() throws RemoteException {
        super(ServerApp.rmiPort);
    }

    public IDeplymentImpl(int port) throws RemoteException {
        super(port);
    }

    private void getAllFilesBy(List<File> listFiles, File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            listFiles.add(file);
            return;
        }
        for (File f : file.listFiles()) {
            getAllFilesBy(listFiles, f);
        }
    }

    @Override
    public ArrayList<JarInfomation> fetchAllJarInformations() throws RemoteException {
        ArrayList<JarInfomation> al = new ArrayList<JarInfomation>();
        String pathname = System.getProperty("user.dir") + "/client";
        int len = pathname.length();
        File file = new File(pathname);
        List<File> listFiles = new ArrayList<File>();
        getAllFilesBy(listFiles, file);
//        file = new File(System.getProperty("user.dir") + "/org");
//        getAllFilesBy(listFiles, file);
        for (File f : listFiles) {
            JarInfomation ji = new JarInfomation();
            ji.setFilename(f.getAbsolutePath().substring(len));
            ji.setLastmodified(f.lastModified());
            al.add(ji);
        }
        Collections.sort(al, new Comparator() {

            @Override
            public int compare(Object arg0, Object arg1) {
                return 0;
            }
        });
        return al;
    }

    @Override
    public byte[] fetchJarBy(String filename) throws RemoteException {
        BufferedInputStream input = null;
        byte[] buffer = null;
        try {
            String pathname = System.getProperty("user.dir") + "/client";
            File file = new File(pathname + filename);
            if (!file.exists()) {
                return null;
            }
            buffer = new byte[(int) file.length()];
            input = new BufferedInputStream(new FileInputStream(file));
            input.read(buffer, 0, buffer.length);
            input.close();
        } catch (Exception ex) {
            log.error(ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return buffer;
    }

    public byte[] fetchJarBy(String filename, int pk_num) throws RemoteException {
        FileInputStream input = null;
        byte[] buffer = null;
        try {
            String pathname = System.getProperty("user.dir") + "/client";
            File file = new File(pathname + filename);
            if (!file.exists()) {
                return null;
            }
            int start = pk_num * 1000;
            int len = (int) Math.min(1000, file.length() - start);
            buffer = new byte[len];
            input = new FileInputStream(file);
            input.skip(start);
            input.read(buffer, 0, len);
            input.close();
        } catch (Exception ex) {
            log.error(ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return buffer;
    }

    public List<String> getLogInData(String fileNo) throws RemoteException {
        List list = new ArrayList();
        try {
            File file = new File(System.getProperty("user.dir") + "/pic/" + fileNo + ".properties");
            if (!file.exists()) {
                System.out.println("file not exists");
                return list;
            }
            FileInputStream fis = new FileInputStream(file);
            Properties props = new Properties();
            props.load(new BufferedInputStream(fis));
            String userName = props.getProperty("userName");
            String userPass = props.getProperty("userPass");
            list.add(userName);
            list.add(userPass);
            fis.close();
            file.delete();
        } catch (FileNotFoundException e) {
            log.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e);
        }
        return list;
    }

    public boolean isServerStarted() throws RemoteException {
        return ServerApp.getServerStartTime() > 0;
    }

    public void stopService() throws RemoteException {
        try {
            HibernateUtil.closeSessionFactory();
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
