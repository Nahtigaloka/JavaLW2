package javalr2;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class JavaLR2 {
    public static void main(String[] args) {
        String exp = "22/x+113*2-34/y+(210/5+24*56)-100";
        List<DifType> exps=expCheck(exp);
        DifTypeBuff expBuff=new DifTypeBuff(exps);
        //x=11, y=2
        System.out.println(EndExps(expBuff));//1497.0
    }
    
    //типы лексем
    public enum TypeOfElem{
        bracket_l, bracket_r, plus_n, minus_n, mult_n, div_n,
        num, StrEnd;//(, ), +, -, *, /, число, конец выражения.
    }
    
    //Класс для описания каждой отдельной лексемы
    public static class DifType{
        TypeOfElem type;//тип лексемы
        String val;//ее представление в изначальном выражении
        
        public DifType(TypeOfElem type, String val){
            this.type = type;
            this.val = val;
        }
        
        public DifType(TypeOfElem type, Character val){
            this.type = type;
            this.val = val.toString();
        }
        
    }
    
    //класс, который упростит работу с массивом лексем при вычислислении выражения
    public static class DifTypeBuff{
        private int pos;
        
        public List<DifType> exps;
        public DifTypeBuff(List<DifType> exps){
            this.exps=exps;
        }
        
        public DifType next() {
            return exps.get(pos++);
        }
        
        public void back() {
            pos--;
        }
        
        public int getPos(){
            return pos;
        }
    }
    
    //функция, анализирующая всю строку-выражение и выдающая массив лексем
    public static List<DifType> expCheck(String exp){
        ArrayList<DifType> exps = new ArrayList<>();
        int pos = 0;
        char chr;
        while(pos < exp.length()) {
            char ch = exp.charAt(pos);
            switch(ch){
                case '(':
                    exps.add(new DifType(TypeOfElem.bracket_l, ch));
                    pos++;
                    continue;
                case ')':
                    exps.add(new DifType(TypeOfElem.bracket_r, ch));
                    pos++;
                    continue;
                case '+':
                    exps.add(new DifType(TypeOfElem.plus_n, ch));
                    pos++;
                    continue;
                case '-':
                    exps.add(new DifType(TypeOfElem.minus_n, ch));
                    pos++;
                    continue;
                case '*':
                    exps.add(new DifType(TypeOfElem.mult_n, ch));
                    pos++;
                    continue;
                case '/':
                    exps.add(new DifType(TypeOfElem.div_n, ch));
                    pos++;
                    continue;
                default:
                    if((ch<='9' && ch>='0') || ch=='.'){
                        StringBuilder sb=new StringBuilder();
                        
                        do{
                            sb.append(ch);
                            pos++;
                            if(pos>=exp.length()){
                                break;
                            }
                            ch = exp.charAt(pos);
                        }while((ch<='9' && ch>='0') || ch=='.');
                        
                        exps.add(new DifType(TypeOfElem.num, sb.toString()));
                    }
                    else{
                        //работа с переменными, если они есть в выражении
                        if(ch>='a' && ch <='z'){
                            Scanner in = new Scanner(System.in);
                            System.out.print("Input a number for " + ch +": ");
                            String num=in.nextLine();
                            int npos=0;
                            chr = num.charAt(npos);
                            if((chr<='9' && chr>='0' ) || chr=='.')
                            {
                                StringBuilder sbr=new StringBuilder();
                                do{
                                    sbr.append(chr);
                                    npos++;
                                    if(npos>=num.length()){
                                        chr=' ';
                                        break;
                                }
                                chr = num.charAt(npos);
                                }while((chr<='9' && chr>='0')||chr=='.');
                                if(chr!=' '){
                                    throw new RuntimeException("Unknown character: " + chr);
                                }
                                exps.add(new DifType(TypeOfElem.num, sbr.toString()));
                            }    
                        }
                        else if(ch!=' '){
                             throw new RuntimeException("Unknown character: " + ch);
                        }
                        pos++;
                    }
            }
            
        }
        exps.add(new DifType(TypeOfElem.StrEnd, ""));
        return exps;
    }
    
    //вычисление выражения реализовано методом рекурсивного спуска.
    //Начиная с EndExps,вызываем процедуры вплоть до NumBracket.
    //Сами вычисления будут проходит по правилам арифметики, 
    //т.е. вычисления в скобках, далее умножение и деление, потом сложение и вычитание.(numbracket, multdiv, plusminus соответственно)   
    public static double EndExps(DifTypeBuff exps){
        DifType exp= exps.next();
        if(exp.type==TypeOfElem.StrEnd){
            return 0;
        }
        else{
            exps.back();
            return PlusMinus(exps);
        }
    }
    
    public static double PlusMinus(DifTypeBuff exps){
       double val=MultDiv(exps);
        while(true){
            DifType exp=exps.next();
            switch(exp.type){
                case plus_n:
                    val+=MultDiv(exps);
                    break;
                case minus_n:
                    val-=MultDiv(exps);
                    break;
                case StrEnd:case bracket_r:
                    exps.back();
                    return val;
                default:
                    throw new RuntimeException("Unexpected " + exp.val + " at position: " + exps.getPos());
            }
        }
    }
    
    public static double MultDiv(DifTypeBuff exps){
        double val=NumBracket(exps);
        while(true){
            DifType exp=exps.next();
            switch(exp.type){
                case mult_n:
                    val*=NumBracket(exps);
                    break;
                case div_n:
                    val/=NumBracket(exps);
                    break;
                case StrEnd:case bracket_r:
                 case plus_n: case minus_n:
                     exps.back();
                     return val;
                default:
                    throw new RuntimeException("Unexpected " + exp.val + " at position: " + exps.getPos());     
            }
        }
    }
    
    public static double NumBracket(DifTypeBuff exps){
        DifType exp= exps.next();
        switch(exp.type){
            case num:
                return Double.parseDouble(exp.val);
            case bracket_l:
                double val = PlusMinus(exps);
                exp = exps.next();
                if(exp.type!=TypeOfElem.bracket_r){
                    throw new RuntimeException("Unexpected " + exp.val + " at position: " + exps.getPos());
                }
                return val;
            default:
                throw new RuntimeException("Unexpected " + exp.val + " at position: " + exps.getPos());
        }
    }
}

