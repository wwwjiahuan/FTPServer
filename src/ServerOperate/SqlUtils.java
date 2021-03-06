package ServerOperate;
import java.sql.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * Created by Ken on 2017/7/4.
 */
public class SqlUtils {

    private static   Connection mysqlconn;
    private static Statement statement;
    private static  ResultSet resultSet;//保留查询结果
    private static LinkedList<String[]> resultList;
    private static int RowCount=0;
    private static String[] QueryColName;
    public SqlUtils() throws ClassNotFoundException, SQLException {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://112.74.58.116:3306/FTPDepot?useUnicode=true&characterEncoding=utf8";
        String user = "FTPUser";
        String password = "admin";
        Class.forName(driver);//加载驱动
        mysqlconn=DriverManager.getConnection(url,user,password);
        statement=mysqlconn.createStatement();
        statement.setQueryTimeout(4);//设置超时
        resultList=new LinkedList<>();
        QueryColName=new String[]{"User","FileName","SrcPath","DesPath","TransferMode","FilePointer","FileSize"};
    }

    //执行查询的SQL
    public static LinkedList QuerySQL(String SQL) throws SQLException {
        resultList.clear();
        try {
            resultSet=statement.executeQuery(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL查询错误 QuerySQL :"+SQL);
        }

        while (resultSet.next()){//next移动行数指针
            String string[]=new String[7];
            for (int i=1;i<=7;i++)
                string[i-1]=resultSet.getString(i);
            resultList.add(string);//保存当前的一行记录
            RowCount++;
        }
        System.out.println("获得查询行数:"+RowCount);
        return resultList;
    }


    public static String getElement(String elementName,int Row){
        System.out.println("查询元素名称:"+elementName +" 行数:"+Row);
        HashMap<String,String> element[]=new HashMap[RowCount];
        for (int i = 0; i <RowCount; i++) {
            element[i]=new HashMap<String,String>();
        }

        //把resultList换成map
        for (int i = 0; i <RowCount ; i++) {
            for (int j = 0; j < 7; j++) {
//                System.out.print(resultList.get(i)[j] +"\t");
//                System.out.print(QueryColName[j] +"\t");
                element[i].put(QueryColName[j],resultList.get(i)[j]);
            }
        }
        return element[Row].get(elementName);
    }


    //获得查询的行数
    public static int getRowCount(){
        return RowCount;
    }
    public static LinkedList<String[]> getResultList(){
        return resultList;
    }
    //执行更新的SQL
    public static boolean UpdateSQL(String SQL){
        try {
            statement.executeUpdate(SQL);
        } catch (SQLException e) {
            System.out.println("SQL更新错误 UpdateSQL "+SQL);
            return false;
        }
        return true;
    }



    //判断用户是否合法登录
    public static boolean isLegalLogin(String user,String password) throws SQLException {
        String sql="SELECT * FROM FTPDepot.User WHERE user='"+user+"' AND PassWord='"+password+"'";
        ResultSet RS = null;
        try {
            RS = statement.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("登录非法 请检查"+sql);
            return false;
        }
        System.out.println("SQL执行成功 "+sql);
        return RS.next();
    }



    //插入语句
    public static void InsertSQL(String tasklist[]){
        String SQL=new String("INSERT INTO FTPDepot.TaskList VALUES (");
        for (int i = 0; i <7; i++) {
            SQL=SQL+"'"+tasklist[i].replace("\\","\\\\")+"'";
            if (i<6)
                SQL=SQL+",";
        }
        SQL=SQL+")";
        UpdateSQL(SQL);
    }

    //修改文件Pionter语句
    public static void PionterSQL(String tasklist[]) throws SQLException {
        if(!isExistTask(tasklist))//如果任务不存在，直接加入记录
            {
                InsertSQL(tasklist);
                System.out.println("当前任务记录不存在，不用更新，已经新插入记录");
                return;
            }

        String SQL=new String("UPDATE FTPDepot.TaskList SET FilePointer=");
        SQL=SQL+"'"+tasklist[5]+"'";
        SQL=SQL+"WHERE USER ='"+tasklist[0]+"'";
        SQL=SQL+"AND FileName='"+tasklist[1]+"'";
        SQL=SQL+"AND TransferMode='"+tasklist[4]+"'";
        UpdateSQL(SQL);
        System.out.println("FilePointer 成功修改为"+tasklist[5]);
    }


    //查询当前任务是否存在
    public static boolean isExistTask(String tasklist[]) throws SQLException {
        String SQL=new String("SELECT * FROM FTPDepot.TaskList ");
        SQL=SQL+" WHERE USER ='"+tasklist[0]+"'";
        SQL=SQL+"AND FileName='"+tasklist[1]+"'";
        SQL=SQL+"AND TransferMode='"+tasklist[4]+"'";
        if (QuerySQL(SQL).isEmpty())
            return false;
        else
            return true;
    }

    //删除任务
    public static boolean DeleteSQL(String tasklist[]) throws SQLException {
        if (isExistTask(tasklist)) {
            String SQL = new String("DELETE FROM FTPDepot.TaskList WHERE ");
            SQL = SQL + "User= '" + tasklist[0] + "'";
            SQL = SQL + "AND FileName= '" + tasklist[1] + "'";
            SQL = SQL + "AND TransferMode= '" + tasklist[4] + "'";
            UpdateSQL(SQL);
            System.out.println("当前任务删除成功");
            return true;
        } else
            System.out.println("当前任务不存在，不用删除");

        return true;
    }
}
