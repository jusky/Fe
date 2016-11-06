package callPic;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by yellowsea on 2016/8/9.
 */
public class GetData {
    public  float a;
    public  float b;
    public  float c;
    public  float Rand(float a2){
        Random random=new Random();
        a2=random.nextFloat()*1000-500;
        return a2;
    }

    public float Rand1(float b2){
        Random random1=new Random();
        b=random1.nextFloat()*1000-500;
        return b2;
    }
    public float Rand2(float c2){
        Random random2=new Random();
        c2=random2.nextFloat()*80;
        return c2;
    }
    public int vizsize[]=new int[100];                     //这个地方写死了.。。。。。。。。。。。。想办法改下


    public void BuildXMLDoc(ArrayList<Node> data1, ArrayList<Edge> data2) throws Exception{

        Document doc= DocumentHelper.createDocument();
        Element gexf=doc.addElement("gexf","http://www.gexf.net/1.2draft");//根节点
        gexf.addAttribute("version", "1.2");
        gexf.addNamespace("viz","http://www.gexf.net/1.2draft/viz");
        gexf.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        gexf.addAttribute("xsi:schemaLocation", "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd");
        Element graph=gexf.addElement("graph");
        graph.addAttribute("defaultedgetype", "undirected").addAttribute("mode", "static");
        Element attributes=graph.addElement("attributes");
        attributes.addAttribute("class", "node").addAttribute("mode", "static");
        Element attribute1=attributes.addElement("attribute");
        Element attribute2=attributes.addElement("attribute");
        attribute1.addAttribute("id","phone_class").addAttribute("title", "Phone Class").addAttribute("type", "integer");
        attribute2.addAttribute("id", "number").addAttribute("title", " Number").addAttribute("type", "string");
        Element nodes=graph.addElement("nodes");
        Element edges=graph.addElement("edges");
        for(Edge d:data2){
            int id1,id2=0;
            id1=Integer.parseInt(d.getSID());
            id2=Integer.parseInt(d.getReID());
            vizsize[id1]++;
            vizsize[id2]++;


        }
        for(Node d : data1){
            String a1=String.valueOf(Rand(a));
            String b1=String.valueOf(Rand1(b));
            int id=Integer.parseInt(d.getUserID());

            if(vizsize[id]!=0){
                String d1=String.valueOf(vizsize[id]*6);
                Element node=nodes.addElement("node");
                node.addAttribute("id", d.getUserID()).addAttribute("label", d.getUserName());
                Element attvalues=node.addElement("attvalues");
                Element attvalue1=attvalues.addElement("attvalue");
                Element attvalue2=attvalues.addElement("attvalue");
                attvalue1.addAttribute("for", "phone_class").addAttribute("value", d.getPhoneID());
                attvalue2.addAttribute("for", "number").addAttribute("value", d.getNum());
                Namespace ns=new Namespace("viz","http://www.gexf.net/1.2draft/viz");
                Element size=node.addElement(new QName("size",ns));
                size.addAttribute("value", d1);
                Element position=node.addElement(new QName("position",ns));
                position.addAttribute("x", a1).addAttribute("y", b1);
                Element color=node.addElement(new QName("color",ns));
                color.addAttribute("r", "235").addAttribute("g", "81").addAttribute("b","72");

            }
        }
        for(Edge d:data2){
            Element edge=edges.addElement("edge");
            edge.addAttribute("id", d.getConID()).addAttribute("source", d.getSID()).addAttribute("target", d.getReID());
            Element attvalues=edge.addElement("attvalues");
            Element attvalue=attvalues.addElement("attvalue");
            attvalue.addAttribute("for", "Fre").addAttribute("value", d.getFre());



        }

        //输出books.xml文件
        //使xml文件 缩进效果
        OutputFormat format=OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        File file=new File(System.getProperty("user.dir") + "/WebRoot/Call.gexf");
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        writer.write(doc);
    }
    public static void generate(String path){
        try{
            GetData G2=new GetData();
            System.out.println("正在生成Call.gexf文件");
            G2.BuildXMLDoc(Call_Record.transfer(path),call.transfer(path));

        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("D:/桌面/Call.gexf 文件已生成");

    }
}
