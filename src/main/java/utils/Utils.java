package utils;

import dao.MybatisDao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
import java.util.Properties;

/**
 * Created by yangz on 2017/9/1 10:08.
 */
public class Utils {
    private static MybatisDao dao = null;

    public static MybatisDao getDao() throws Exception {
        if (dao == null) {
            InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            // 是否自动提交
            SqlSession session = sqlSessionFactory.openSession(true);
            dao = session.getMapper(MybatisDao.class);
        }
        return dao;

    }
    //读取资源文件,并处理中文乱码
    public static void readPropertiesFile(String filename)
    {
        Properties properties = new Properties();
        try
        {
            InputStream inputStream = new FileInputStream(filename);
            properties.load(inputStream);
            inputStream.close(); //关闭流
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String username = properties.getProperty("username");
        String passsword = properties.getProperty("password");
        String chinese = properties.getProperty("chinese");
        System.out.println(username);
        System.out.println(passsword);
        System.out.println(chinese);
    }

    //读取XML文件,并处理中文乱码
    public static void readPropertiesFileFromXML(String filename)
    {
        Properties properties = new Properties();
        try
        {
            InputStream inputStream = new FileInputStream(filename);
            properties.loadFromXML(inputStream);
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String username = properties.getProperty("username");
        String passsword = properties.getProperty("password");
        String chinese = properties.getProperty("chinese"); //XML中的中文不用处理乱码，正常显示
        System.out.println(username);
        System.out.println(passsword);
        System.out.println(chinese);
    }

    //写资源文件，含中文
    public static void writePropertiesFile(String filename)
    {
        Properties properties = new Properties();
        try
        {
            OutputStream outputStream = new FileOutputStream(filename);
            properties.setProperty("username", "myname");
            properties.setProperty("password", "mypassword");
            properties.setProperty("chinese", "中文");
            properties.store(outputStream, "author: yangzhao2005@qq.com");
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //写资源文件到XML文件，含中文
    public static void writePropertiesFileToXML(String filename)
    {
        Properties properties = new Properties();
        try
        {
            OutputStream outputStream = new FileOutputStream(filename);
            properties.setProperty("username", "myname");
            properties.setProperty("password", "mypassword");
            properties.setProperty("chinese", "中文");
            properties.storeToXML(outputStream, "author: shixing_11@sina.com");
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
