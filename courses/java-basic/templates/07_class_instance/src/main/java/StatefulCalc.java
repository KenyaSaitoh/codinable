class StatefulCalc {
    int x; //【1】
    int y;

    public StatefulCalc(int x, int y) { //【2】
        this.x = x;
        this.y = y;
    }

    int add() { //【3】
        int result = x + y;
        return result;
    }

    int subtract() {
        int result = x - y;
        return result;
    }
}