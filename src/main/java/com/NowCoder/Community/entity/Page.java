package com.NowCoder.Community.entity;
/*
    封装分页相关的信息
 */
public class Page {
    //当前的页码
    private int cur=1;
    //显示的上限
    private int lim=10;
    //数据的总数(用于计算总的页数)
    private int rows;
    //查询路径(用来复用分页链接)
    private String path;

    @Override
    public String toString() {
        return "Page{" +
                "cur=" + cur +
                ", lim=" + lim +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        if (cur>=1)
        {
            this.cur = cur;
        }
    }

    public int getLim() {
            return lim;
    }

    public void setLim(int lim) {
        if (lim>=1 && lim<=100)
            this.lim = lim;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    //获取当前页面的起始行
    public int getOffset()
    {
        return (cur-1) *lim;
    }
    //获取总的页数
    public int getTotal()
    {
        if (rows%lim==0) return rows/lim;
        else return rows/lim+1;
    }
    //获取底部起始页码
    public int getFrom()
    {
        int from=cur-2;
        if (from<1) return 1;
        else return from;
    }
    //获取底部截止页码
    public int getTo()
    {
        int to=cur+2;
        int total=getTotal();
        return (to>total) ? total : to;

    }

}
