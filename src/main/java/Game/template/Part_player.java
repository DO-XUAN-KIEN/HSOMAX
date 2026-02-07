package Game.template;

public class Part_player {

    public short type;
    public short part;
    public Part_player(){}
    public Part_player(int type, int part){
        this.type = (byte) type;
        this.part = (byte) part;
    }
}
