package com.example.walkwalk.mysqlDB;
import android.util.Log;
import java.sql.*;
import java.sql.PreparedStatement;
import java.sql.Statement;
public class mysqlDB {
    private static String TAG = "MySQL";
    private static String ip = "cdb-a9p7ajpe.cd.tencentcdb.com";   //外网ip地址
//    private static String ip = "localhost";   //外网ip地址
    private static String port = "10051";   //端口地址
//    private static String port ="3306";   //端口地址
    private static String user = "root";    //用户
    private static String password = "yzqYZQ19971023";  //密码
//    private static String password = "19971023";  //密码
    private static String url = "jdbc:mysql://" + ip + ":" + port + "/";    //连接的url
    private static String dbName = "WalkWalk";  //数据库名

    public static Connection con = null;
    public static PreparedStatement stmt = null;
    public static ResultSet rs = null;  //查询集

    //数据库连接
    public static Connection getConnection(){
        Connection conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn=DriverManager.getConnection(url+dbName,user,password); //建立连接
            Log.d(TAG, "数据库连接成功！");
        }catch (SQLException ex){
            ex.printStackTrace();
            Log.d(TAG, "数据库连接异常");
        }catch(ClassNotFoundException ex){
            ex.printStackTrace();
            Log.d(TAG, "数据库缺少依赖");
        }

        return conn;
    }

    //关闭数据库
    public static void close(ResultSet rs,Statement stmt,Connection con){
        try{
            if(rs!=null){
                rs.close();
            }
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        try{
            if(stmt!=null){
                stmt.close();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        try{
            if(con!=null){
                con.close();
            }
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
}
