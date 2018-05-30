package SourceCode;

public class test {

    int MAX = 3;

    void foo()
    {
        int x = source();
        if (x < MAX)
        {
            int y = 2 * x;
            sink(y);
        }
    }

    int source(){
        return 0;
    }

    void sink(int a){
        int b = a+1;
    }
}

/*

void foo()
    {
        int x = source();
        if (x < MAX)
        {
            int y = 2 * x;
            switch(x){
                case 0:
                    y=1;
                case 1:
                    y=2;
                default:
                    y=3;
            }
            sink(y);
        }
    }



void foo()
    {
        int x = source();
        if (x < MAX)
        {
            int y = 2 * x;
            sink(y);
        }
    }
 */
