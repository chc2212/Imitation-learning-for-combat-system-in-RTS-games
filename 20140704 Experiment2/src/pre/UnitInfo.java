package pre;


public class UnitInfo {
	int id; // 유닛의 ID ( 출력 1번 )
	int type; // -1이면 아군 드라군, -2면 아군 질럿, -3이면 적군 드라군, -4면 적군 질럿
	int x; // 유닛의 x좌표
	int y; // 유닛의 y좌표
	int health;
	int orderID; // 유닛의 현재 공격 상태 ( 출력 3번 )
	int coolDown;
	
	
	int position; // 좌표가 변환되어 맵에 출력된 위치 값. ( 출력 2번 )
	int nextDirection; // 다음 이동 방향. ( 출력 4번 )
	int influence; // 영향력 값.
	
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
