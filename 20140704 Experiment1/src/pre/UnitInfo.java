package pre;


public class UnitInfo {
	int id; // ������ ID ( ��� 1�� )
	int type; // -1�̸� �Ʊ� ���, -2�� �Ʊ� ����, -3�̸� ���� ���, -4�� ���� ����
	int x; // ������ x��ǥ
	int y; // ������ y��ǥ
	int health;
	int orderID; // ������ ���� ���� ���� ( ��� 3�� )
	int coolDown;
	
	
	int position; // ��ǥ�� ��ȯ�Ǿ� �ʿ� ��µ� ��ġ ��. ( ��� 2�� )
	int nextDirection; // ���� �̵� ����. ( ��� 4�� )
	int influence; // ����� ��.
	
	boolean copyUnit = false;
	
	public UnitInfo(int _id, int _type, int _x, int _y, int _health, int _orderID, int _coolDown, int _nexDirection, boolean _copyUnit)
	{
		id = _id;
		type = _type;
		x = _x;
		y = _y;
		health = _health;
		orderID = _orderID;
		coolDown = _coolDown;
		nextDirection = _nexDirection;
		copyUnit = _copyUnit;
	}
	
	public void setCoolDown(int _coolDown){ coolDown = _coolDown; }
	public void setCopyUnit(boolean _copyUnit){ copyUnit = _copyUnit; }
	public void setOrderID(int _orderID){ orderID = _orderID; }
	
	public int getPosition(){ return position; }
	public int getNextDirection(){ return nextDirection; }
	public int getOrderID(){ return orderID; }
	public int getType(){ return type; }
	public boolean getCopyUnit(){ return copyUnit; }
}
