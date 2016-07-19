package jnibwapi;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale.Builder;

import pre.Frames;
import pre.HashTable;
import pre.PreProcess;
import pre.UnitInfo;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

//����2

/**
 * Example Java AI Client using JNI-BWAPI.
 * 
 * Executes a 5-pool rush and cheats using perfect information.
 * 
 * Note: the agent often gets stuck when attempting to build the spawning pool. It works best on
 * maps where the overlord spawns with plenty of free space around it.
 */
public class ExampleAIClient implements BWAPIEventListener {
	
	/** reference to JNI-BWAPI */
	private JNIBWAPI bwapi;
	
	/** used for mineral splits */
	private HashSet<Integer> claimed = new HashSet<Integer>();
	
	//------------------�μ��� �߰� �ڵ�----------------------//
	
	/** ���� ����Ǵ� ���ӳ��� ���ֵ鿡 ���� ������ ����.*/ 
	ArrayList<Frames> currentPlay;
	
	/** ���ϵ� ����� ���� ���� ��ġ�ϴ� ������ */
	Frames similarFrame;
	
	/** ���� ������ ã��*/
	int frameIndex;
	
	int similarFrameIndex;
	
	/** ���� ���� ������*/
	private int gameFrame;
	
	private int startFrame;
	private boolean isStartFrame = false;
	
	/** ���� ������ ����*/
	private final int GAMEFRAMECOUNT = 3;
	
	/** �ִ� �ػ� ���� ��..*/
	private final int EXPAND_LEVEL_MAX = 5;
	
	/** �ð� ����*/
	long start = 0;
	long end = 0;
	
	long startOrderTime, endOrderTime;
	
	public ArrayList<Float> saveTimer = new ArrayList<Float>();
	public ArrayList<Float> expandAmount = new ArrayList<Float>();
	public ArrayList<Integer> hashLevel = new ArrayList<Integer>();
	public ArrayList<Integer> totalReferenceScene = new ArrayList<Integer>();
	public ArrayList<Integer> currentFrameNumber = new ArrayList<Integer>();
	
	public ArrayList<Float> makeIMTimer = new ArrayList<Float>();
	public ArrayList<Float> compareFrameTimer = new ArrayList<Float>();
	public ArrayList<Float> orderTimer = new ArrayList<Float>();
	public ArrayList<Float> hashTimer = new ArrayList<Float>();
	
	public static long frameTimer = 0;
	
	public static int sumExpand = 0;
	
	public static int sumMyUnit = 0;
	
	public boolean firstStart = true;
	
	public int gameNumber = 0;
	/**
	 * Create a Java AI.
	 */
	public static void main(String[] args) {
		new ExampleAIClient();
	}
	
	/**
	 * Instantiates the JNI-BWAPI interface and connects to BWAPI.
	 */
	public ExampleAIClient() {
		bwapi = new JNIBWAPI(this, true);
		bwapi.start();
	}
	/**
	 * Connection to BWAPI established.
	 */
	@Override
	public void connected() {
		System.out.println("Connected");
	}
	
	/**
	 * Called at the beginning of a game.
	 */
	@Override
	public void matchStart() {
		System.out.println("Game Started");
		
		
		bwapi.enableUserInput();
		//bwapi.enablePerfectInformation();
		bwapi.setGameSpeed(0);

		
		claimed.clear();

		/** Insert Code �μ�*/
		if(firstStart == false)
		{
			saveTimer.clear();
			expandAmount.clear();
			currentPlay.clear();
			
			hashLevel.clear();
			totalReferenceScene.clear();
			currentFrameNumber.clear();
			makeIMTimer.clear();
			compareFrameTimer.clear();
			orderTimer.clear();
			hashTimer.clear();

		}
		
		if(gameNumber % 40 == 0 && gameNumber != 0)
		{
			//if(gameNumber == 200)
			//	PreProcess.LIMITLINE = 200000; // ���� �� ��� �ڵ�ȭ.
			//if(gameNumber == 200)
			//	PreProcess.LIMITLINE = 50000;		
			//if(gameNumber == 400)
			//	PreProcess.LIMITLINE = 25000;
			//if(gameNumber == 600)
			//	PreProcess.LIMITLINE = 25000;
			
			/*if(gameNumber == 40)
				PreProcess.maxReferenceCount = 125; // ���� ���� �ڵ�ȭ.
			if(gameNumber == 80)
				PreProcess.maxReferenceCount = 250; // ���� ���� �ڵ�ȭ.
			if(gameNumber == 120)
				PreProcess.maxReferenceCount = 500; // ���� ���� �ڵ�ȭ.
			if(gameNumber == 160)
				PreProcess.maxReferenceCount = 2000; // ���� ���� �ڵ�ȭ.*/
			
			
			firstStart = true;
			
			Frames.noneReference = null;
			Frames.noneReference = new ArrayList<Integer>();
			
			
			PreProcess.frameArray = null;
			PreProcess.frameArray = new ArrayList<Frames>(); // �޸� �ٽ� �Ҵ�..
			
			PreProcess.hashtable = null;
			
			PreProcess.hashtable = new HashTable[100][64][64];
			
			for(int i = 0; i < 100; i++){
				for(int j = 0; j < 64; j++){
					for(int k = 0; k < 64; k++)
					{
						PreProcess.hashtable[i][j][k] = new HashTable();
					}
				}
			} // hashtable �ʱ�ȭ
			
			PreProcess.nonReferenceFrameNumber = null;
			PreProcess.nonReferenceFrameNumber = new ArrayList<Integer>();
			
			PreProcess.totalMyLocalInfluenceMap = null;
			PreProcess.totalEnLocalInfluenceMap = null;
			
			PreProcess.totalMyLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			PreProcess.totalEnLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			
			PreProcess.lineNumber = 0;
			
			System.out.println("������ �� �缳�� �մϴ�. ���� ���� Ƚ�� : "+gameNumber);

		} // ���� �ѹ� 40�϶� ���带 �ٽ� ����.
		
		
		try {
			if(firstStart)
			{
				pre.PreProcess.ProcessBuild();
				firstStart = false;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentPlay = null;
		
		currentPlay = new ArrayList<Frames>();
		frameIndex = -1;
		similarFrameIndex = 0;
		gameFrame = 0;
		
		isStartFrame = false;
		gameNumber++;
		
		PreProcess.unitDistancehashValue = 0;
		//PreProcess.hashValue = 400;
		PreProcess.differUnitCount = 0;

		/////////////////////////// ���� �߰�  ///////////////////////////
		//bwapi.build(unitID, tx, ty, typeID);

	}
	
	/**
	 * Called each game cycle.
	 */
	@Override	
	public void matchFrame() {
		
		/*for(Unit unit : bwapi.getMyUnits())
		{
			//System.out.println(unit.getTypeID());
			if(unit.getTypeID() == UnitTypes.Protoss_Probe.getID())
			{
				//System.out.println("DDDD");
				if(unit.getOrderID() == 14 || unit.getOrderID() == 10)
				{
					if(unit.isCarryingMinerals())
					{
						bwapi.attack(unit.getID(), 100, 100);
						System.out.println("���� Ÿ�� ��ǥ : "+unit.getTargetX()+","+ unit.getTargetY());
						System.out.println("��������");
						break;
					}
					else if(!unit.isCarryingGas())
					{
						bwapi.attack(unit.getID(), 101, 101);
						System.out.println("���� Ÿ�� ��ǥ : "+unit.getTargetX()+","+ unit.getTargetY());
						System.out.println("��������");
						break;
					}
				}
			}
		}*/

		
		//bwapi.sendText("BBBB");
		
		if(isStartFrame == false)
			startFrame = bwapi.getFrameCount();
		
		isStartFrame = true;
		
		start = System.currentTimeMillis();
		
		gameFrame = bwapi.getFrameCount();
		System.out.println("gameFrame ���� : "+gameFrame);
		
		/*if(gameFrame >= 5000)
			PreProcess.differUnitCount = 1;
		else if(gameFrame >= 10000)
			PreProcess.differUnitCount = 2;
		else if(gameFrame >= 20000)
			PreProcess.differUnitCount = 7;
		*/
		/*bwapi.drawText(5, 10, "Reference Frame :" + similarFrameIndex/3, true);
		bwapi.drawText(5, 20, "Total number of Frames :" + PreProcess.lineNumber, true);*/
		
/*		
		for(Unit unit : bwapi.getMyUnits())
		{
			if(unit.getTypeID() == UnitTypes.Protoss_Dragoon.getID() || unit.getTypeID() == UnitTypes.Protoss_Zealot.getID())
			{
				bwapi.drawText(unit.getX(), unit.getY(), ""+unit.getID(), false);
			}
		}
				
		for(int i = 0; i < bwapi.getMyUnits().size(); i++)
		{
			if(bwapi.getMyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID())
					//|| bwapi.getMyUnits().get(i).getTypeID() == UnitTypes.Protoss_Zealot.getID())
			{
				bwapi.drawText(5, 10, "gameFrame :" + Integer.toString(gameFrame), true);
				bwapi.drawText(5, 20, "ID :" + Integer.toString(bwapi.getMyUnits().get(i).getID()), true);
				bwapi.drawText(5, 30, "TYPE :" + Integer.toString(bwapi.getMyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() ? -1 : -2), true);
				bwapi.drawText(5, 40, "X :" + Integer.toString(bwapi.getMyUnits().get(i).getX()), true);
				bwapi.drawText(5, 50, "Y :" + Integer.toString(bwapi.getMyUnits().get(i).getY()), true);
				bwapi.drawText(5, 60, "HEALTH :" + Integer.toString(bwapi.getMyUnits().get(i).getHitPoints() + bwapi.getMyUnits().get(i).getShields()), true);
				bwapi.drawText(5, 70, "ISATTACKING :" + Integer.toString(bwapi.getMyUnits().get(i).isAttacking() ? 1 : 0), true);
				
				bwapi.drawText(5, 80, "COOLDOWNTIME :" + Integer.toString(bwapi.getMyUnits().get(i).getGroundWeaponCooldown()), true);
				bwapi.drawText(5, 90, "isStartingAttack :" + Integer.toString(bwapi.getMyUnits().get(i).isStartingAttack() ? 1 : 0), true);
				bwapi.drawText(5, 100, "isAttacking :" + Integer.toString(bwapi.getMyUnits().get(i).isAttacking() ? 1 : 0), true);
				bwapi.drawText(5, 110, "getOrderID :" + Integer.toString(bwapi.getMyUnits().get(i).getOrderID()), true);		
				bwapi.drawText(5, 120, "Similar Frame :" + similarFrameIndex, true);
				break;
			}
		}
		for(int i = 0; i < bwapi.getEnemyUnits().size(); i++)
		{
	
			if(bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() 
					|| bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Zealot.getID())
			{
				if(bwapi.getEnemyUnits().get(i).isVisible() == false)
					continue;
				
				bwapi.drawText(300, 10, "gameFrame :" + Integer.toString(gameFrame), true);
				bwapi.drawText(300, 20, "ID :" + Integer.toString(bwapi.getEnemyUnits().get(i).getID()), true);
				bwapi.drawText(300, 30, "TYPE :" + Integer.toString(bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() ? -1 : -2), true);
				bwapi.drawText(300, 40, "X :" + Integer.toString(bwapi.getEnemyUnits().get(i).getX()), true);
				bwapi.drawText(300, 50, "Y :" + Integer.toString(bwapi.getEnemyUnits().get(i).getY()), true);
				bwapi.drawText(300, 60, "HEALTH :" + Integer.toString(bwapi.getEnemyUnits().get(i).getHitPoints() + bwapi.getEnemyUnits().get(i).getShields()), true);
				bwapi.drawText(300, 70, "ISATTACKING :" + Integer.toString(bwapi.getEnemyUnits().get(i).isAttacking() ? 1 : 0), true);
				
				bwapi.drawText(300, 80, "COOLDOWNTIME :" + Integer.toString(bwapi.getEnemyUnits().get(i).getGroundWeaponCooldown()), true);
				bwapi.drawText(300, 90, "Similar Frame :" + similarFrameIndex, true);
				break;
			}
		}
*/
		///////////////////////////////////////////////////////////////////////////////
		//////////////////////////��������� ȭ�鿡 ��¿� ���� �κ�////////////////////////////
		///////////////////////////////////////////////////////////////////////////////
//		/*for(Unit unit : bwapi.getMyUnits())
//		{
//			if(unit.getTypeID() == UnitTypes.Protoss_Dragoon.getID() || unit.getTypeID() == UnitTypes.Protoss_Zealot.getID())
//			{
//				bwapi.attack(unit.getID(), 1770, 520);
//				System.out.println("TILE X, Y : "+unit.getTileX()+","+unit.getTileY());
//				System.out.println("Target X, Y : "+unit.getTargetX()+","+unit.getTargetY());
//				System.out.println("TargetID : "+unit.getTargetUnitID());
//			}
//		}*/
		
		//if(gameFrame % GAMEFRAMECOUNT == 0)
		//{
			
			for(Unit unit : bwapi.getMyUnits())
			{
				if(unit.isCompleted() == false)
					continue;
				
				if(unit.getTypeID() == UnitTypes.Protoss_Dragoon.getID() || unit.getTypeID() == UnitTypes.Protoss_Zealot.getID())
				{
					sumMyUnit++;
				}
			}
			
			PreProcessAllUnit(); // �Ʊ��� ���� ������ ��ó��. ����Ʈ�� ���� ��ó���Ͽ� �߰���Ŵ
			currentPlay.get(frameIndex).setPosition(); // ������ �� �� ó��
			currentPlay.get(frameIndex).myLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			currentPlay.get(frameIndex).enLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			currentPlay.get(frameIndex).SumInfluence(); // ����� ���� �� ���
			currentPlay.get(frameIndex).ComputeNumberOfTotalUnit(); // ���� �Ʊ� ���� �� ���
			currentPlay.get(frameIndex).InfluenceProcessing(); // ����� �� ����
			currentPlay.get(frameIndex).DistanceAverage();
			
			
			/*try {
				currentPlay.get(frameIndex).WriteFile(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			// ����� ����� �� ã�� ������ ��ȯ
			similarFrame = currentPlay.get(frameIndex).CompareMap(currentPlay.get(frameIndex));
			similarFrameIndex = similarFrame.currentFrame;
			
			
			System.out.println("�������� �� : "+similarFrame.sumInfluence+"  ������� �� : " +currentPlay.get(frameIndex).sumInfluence);
			//System.out.println("�������� �Ʊ� �� : "+similarFrame.sumMYInfluence+"  ������� �Ʊ� �� : " +currentPlay.get(frameIndex).sumMYInfluence);
			//System.out.println("�������� ���� �� : "+similarFrame.sumENInfluence+"  ������� ���� �� : " +currentPlay.get(frameIndex).sumENInfluence);
			
			///////////////////////
			//���Ϲ��� ���� ���� ����� ���ֵ� ���Ͽ� �ۼ��ϱ�
			
			///////////////////////
			
			//System.out.println("���� ����� �� :"+currentPlay.get(frameIndex).sumENInfluence);
			
			/////////////////////
			/** ���� ������ �����ϱ�*/
			/////////////////////
			startOrderTime = System.currentTimeMillis();
			
			int printCount = 0;
			
			for (Unit unit : bwapi.getMyUnits()) 
			{
				if(unit.isCompleted() == false)
					continue;
				
				printCount++;
				
				int x, y, position, orderID, coolDown;
				int temp_X, temp_Y;
				
				if(unit.getTypeID() == UnitTypes.Protoss_Dragoon.getID() || unit.getTypeID() == UnitTypes.Protoss_Zealot.getID())
				{
					x = unit.getX();
					y = unit.getY();
					orderID = unit.getOrderID();
					coolDown = unit.getGroundWeaponCooldown(); 
							
					temp_X = x / PreProcess.DIVIDE_SIZE;
					temp_Y = y / PreProcess.DIVIDE_SIZE;
					
					position = temp_X + temp_Y * PreProcess.SIZE_X; // ������ ���ϴ� ��
					
					boolean isConform = DecideCommander(unit, position, orderID, coolDown, x, y, printCount);
					int expandLevel = 1;
					
					while(isConform == false)
					{
						//����� �� �ػ� ����. �׸��� �ѹ� �� ��� ����
						similarFrame.ExpandInfluence(expandLevel);
						isConform = DecideCommander(unit, position, orderID, coolDown, x, y, printCount);
						expandLevel++;
						
						if(expandLevel == EXPAND_LEVEL_MAX)
						{
							float maxNumber = 0;
							int position_Y = 0;
							int position_X = 0;
							
							for(int i = 0; i < PreProcess.SIZE_Y; i++)
							{
								for(int j = 0; j < PreProcess.SIZE_X; j++)
								{
									if(maxNumber <= currentPlay.get(frameIndex).myLocalInfluenceMap[i][j])
									{
										maxNumber = currentPlay.get(frameIndex).myLocalInfluenceMap[i][j];
										position_Y = i;
										position_X = j;
									}
								}
							}
							sumExpand++;
							//System.out.println("Expand �Ǿ���");
							if(unit.getOrderID() == 14 || unit.getOrderID() == 10);
							else
								bwapi.move(unit.getID(), position_X * PreProcess.DIVIDE_SIZE, position_Y * PreProcess.DIVIDE_SIZE);
							break;
						}
					}
					
					//System.out.println("expandLevel : "+expandLevel);
					
					for(int i = 0; i < similarFrame.unitArray.size(); i++)
					{
						if(similarFrame.unitArray.get(i).getCopyUnit() == true)
						{
							similarFrame.unitArray.remove(i);
							i--;
						} // copy�� ��ü ����
					}
					//true ��ü �Ҹ� �ʿ� copy
				} // if �� ��
			} // for �� ��
			
			
			System.out.println("sumExpand : "+sumExpand+" sumMyUnit :"+sumMyUnit);
			
			
			if(sumExpand >= sumMyUnit * 0.7f)
			{
				//System.out.println("��� expand");
				
				for (Unit unit : bwapi.getMyUnits())
				{
					if(unit.isCompleted() == false)
						continue;
					
					if(unit.getTypeID() == UnitTypes.Protoss_Dragoon.getID() || unit.getTypeID() == UnitTypes.Protoss_Zealot.getID())
					{
						if((gameFrame - startFrame) % 24 == 0)
						{
							if(unit.getOrderID() == 14 || unit.getOrderID() == 10);
							else
							{
								int countNexus = 0;
								for(Unit u : bwapi.getMyUnits())
								{
									if(unit.isCompleted() == false)
										continue;
									
									if(u.getID() == UnitTypes.Protoss_Nexus.getID())
										countNexus++;
								}
								/*if(countNexus <= 1)
								{
									bwapi.attack(unit.getID(), 2100, 130); //�����
									//System.out.println("X, Y :"+unit.getOr+","+unit.getTargetY());
								}
								else if(countNexus >= 2)*/
									bwapi.attack(unit.getID(), 2000, 2000); //�ո���
								//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
								//bwapi.attack(unit.getID(), (bwapi.getMap().getWalkWidth() * 32) / 2, (bwapi.getMap().getHeight() * 32) / 2);
								System.out.println("��� expand�Ǿ� ���� ����");
							}
						}
					}
				}
			}
			
			endOrderTime = System.currentTimeMillis();
			
			/*if((double)sumExpand / (double)sumMyUnit > 0.2)
			{
				PreProcess.hashValue += 50;
				if(PreProcess.hashValue > PreProcess.maxHashValue)
					PreProcess.hashValue = PreProcess.maxHashValue;
			}
			else if((double)sumExpand / (double)sumMyUnit < 0.1)
			{
				PreProcess.hashValue -= 50;

				if(PreProcess.hashValue < PreProcess.minHashValue)
					PreProcess.hashValue = PreProcess.minHashValue;
			}*/
			// �ؽ� ���� ����. ���� ���� ����Ͽ�.. �˸��� ���� ��� ã��. �ӵ��� ����ȭ �ϱ� ����.
			
			//System.out.println("������ �Ʊ� ���� �� : " + sumMyUnit);
			System.out.println("������ FilterLevel: " + (PreProcess.unitDistancehashValue - PreProcess.minUnitDistancehashValue));
			//System.out.println("������ Ȯ�� �� : " + sumExpand);
			//System.out.println("sumExpand�� �Ʊ� �� �������� ���� ���� : " + (double)sumExpand / (double)sumMyUnit);
			
			expandAmount.add(sumExpand / (float)sumMyUnit);
			hashLevel.add((int) (PreProcess.differUnitCount + 1));
			totalReferenceScene.add(Frames.currentReferenceNumber);
			currentFrameNumber.add(gameFrame);
			
			
			makeIMTimer.add(Frames.makeIMTime / 1000.0f);
			compareFrameTimer.add(Frames.compareTime / 1000.0f);
			hashTimer.add(Frames.hashTime / 1000.0f);
			orderTimer.add((endOrderTime - startOrderTime) / 1000.0f);
			
			
			sumExpand = 0;
			sumMyUnit = 0;

			/** Test�� ���Ͽ� �Ἥ Data Ȯ���ϱ�*/
			
			/*try {
				currentPlay.get(frameIndex).WriteFile(0);
				currentPlay.get(frameIndex).WriteFile(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
		//} // ���� ������
		
		end = System.currentTimeMillis();

		//bwapi.drawText(5, 30, "Processing Time :" +(end - start)/1000.0 , true);
		//bwapi.drawText(300, 5, ""+(end - start)/1000.0, true);
		saveTimer.add(((end - start)/1000.0f));
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	} // match Frame
	
	
	
	
	
	
	
	public void PreProcessAllUnit()
	{
		currentPlay.add(new Frames(gameFrame, true));
		frameIndex++;
		for(int i = 0; i < bwapi.getMyUnits().size(); i++)
		{
			if(bwapi.getMyUnits().get(i).isCompleted() == false)
				continue;
			
			if(bwapi.getMyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() 
					|| bwapi.getMyUnits().get(i).getTypeID() == UnitTypes.Protoss_Zealot.getID())
			{
				int id, type, x, y, health, orderID, coolDown;
				id = bwapi.getMyUnits().get(i).getID();
				type =  bwapi.getMyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() ? -1 : -2;
				x = bwapi.getMyUnits().get(i).getX();
				y = bwapi.getMyUnits().get(i).getY();
				health = bwapi.getMyUnits().get(i).getHitPoints() + bwapi.getMyUnits().get(i).getShields();
				orderID = bwapi.getMyUnits().get(i).getOrderID();
				coolDown = bwapi.getMyUnits().get(i).getGroundWeaponCooldown();
				
				currentPlay.get(frameIndex).unitArray.add(new UnitInfo(id, type, x, y, health, orderID, coolDown, 0, false));
				
				/*
				bwapi.drawText(5, 10, "gameFrame :" + Integer.toString(gameFrame), true);
				bwapi.drawText(5, 20, "ID :" + Integer.toString(id), true);
				bwapi.drawText(5, 30, "TYPE :" + Integer.toString(type), true);
				bwapi.drawText(5, 40, "X :" + Integer.toString(x), true);
				bwapi.drawText(5, 50, "Y :" + Integer.toString(y), true);
				bwapi.drawText(5, 60, "HEALTH :" + Integer.toString(health), true);
				bwapi.drawText(5, 70, "ISATTACKING :" + Integer.toString(isAttacking), true);
				*/
				
			} 
		} // �Ʊ� ���� ó��
		
		for(int i = 0; i < bwapi.getEnemyUnits().size(); i++)
		{
			if(bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() 
					|| bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Zealot.getID())
			{
				if(bwapi.getEnemyUnits().get(i).isCompleted() == false)
					continue;
				
				//if(bwapi.getEnemyUnits().get(i).isVisible() == false)
				//	continue;
				// �þ߿��� �Ⱥ��̸� �� ������ üũ���� ����.
				
				int id, type, x, y, health, orderID, coolDown;
				id = bwapi.getEnemyUnits().get(i).getID();
				type =  bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() ? -3 : -4;
				x = bwapi.getEnemyUnits().get(i).getX();
				y = bwapi.getEnemyUnits().get(i).getY();
				health = bwapi.getEnemyUnits().get(i).getHitPoints() + bwapi.getEnemyUnits().get(i).getShields();
				orderID = bwapi.getEnemyUnits().get(i).getOrderID();
				coolDown = bwapi.getEnemyUnits().get(i).getGroundWeaponCooldown();
				
				currentPlay.get(frameIndex).unitArray.add(new UnitInfo(id, type, x, y, health, orderID, coolDown, 0, false));
			}
		} // ���� ���� ó��
	}
	
	// ��ɰ��� �κ�
	public boolean DecideCommander(Unit unit, int position, int orderID, int coolDown, int x, int y, int printCount){
		
		boolean isConform = false;
		
		for(int i = 0; i < similarFrame.unitArray.size(); i++)
		{
			// ���� ������ �����ǰ� ���� ����� �������� ���� �� �������� �������� ������
			if(similarFrame.unitArray.get(i).getType() == -1 || similarFrame.unitArray.get(i).getType() == -2)
			{
				if(similarFrame.unitArray.get(i).getPosition() == position)
				{

					isConform = true;
					// ���� ������ ������ �������� �����Ͽ� ���� �������� �̵���Ŵ.
					// �̵� ����
					// 1. ���� : ���� , ���� : ���� -> ���º��� X
					// 2. ���� : ���� , ���� : �̵� -> ��ٿ� 10 �� �̵�, ��ٿ� 10 �� ���º��� X
					// 3. ���� : �̵� , ���� : ���� -> ��ٿ� 10 �� ���º��� X, ��ٿ� 10 �� ����( - ���� �� Ÿ���� ? )
					// 4. ���� : �̵� , ���� : �̵� -> ������ �̵�
					
					if(similarFrame.unitArray.get(i).getOrderID() == 107) // order�� Hold��
					{
						similarFrame.unitArray.get(i).setOrderID(10);
					}
					if(orderID == 107)
					{
						orderID = 10;
					}
					
					
					if((orderID == 14 || orderID == 10) && (similarFrame.unitArray.get(i).getOrderID() == 14 || similarFrame.unitArray.get(i).getOrderID() == 10))
					{
		
						//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
						//CaptureLine(unit, similarFrame.unitArray.get(i).getNextDirection());
						
						if(unit.isAttacking() == true)
						{
							
						}
						else if(coolDown >= 10 && coolDown <= 23)
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]����: ����, ���� : ���� , CurrentCoolDown : "+coolDown+" ���� ���� ��� : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("���� ���� ������ : "+ similarFrame.currentFrame +" ���� ��ġ [X,Y] : "+unit.getX()+","+unit.getY());
							
							AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
						}
						else if(unit.isAttacking() == false && ((gameFrame - startFrame) % 24 == 0) && coolDown == 0)
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]����: ����, ���� : ���� , CurrentCoolDown : "+coolDown+" ���� ���� ��� : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("���� ���� ������ : "+ similarFrame.currentFrame +" ���� ��ġ [X,Y] : "+unit.getX()+","+unit.getY());
							
							boolean tooClose = false;
							
							for(Unit enemy: bwapi.getEnemyUnits())
							{
								if(250 > Math.sqrt(Math.pow((unit.getX() - enemy.getX()), 2) + Math.pow((unit.getY() - enemy.getY()), 2)))
								{
									tooClose = true;
									break;
								}
							} // �ʹ� �� ������ ������ , ���ݸ���� �ߺ������� ������ �ʱ� ���� �Ÿ��� �缭(����� ��Ÿ��� ������ ��Ÿ��� 230~240����) 250���� ������ ���ݸ���� ������ ����.
							
							if(tooClose == false)
							{
								//System.out.println("�� ������ ��ó�� ���� ���ݸ���� �ٽ� ����");
								AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
							}
							
						} // ���� ���°� ���� ���� �ƴϸ�, ��ǥ�� ������ �ٽ� ��ƾ� �ϹǷ�, ���� ������ ����. �ٵ� ��� ��Ÿ���� 0�̾, ������ ���� ��ų� �ݺ��� �� �����Ƿ� 9������ ���� ����ǰԲ� ��.
						
						break;
						//if(coolDown == 0)
						//	AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
					}
					
					else if((orderID == 14 || orderID == 10) && (similarFrame.unitArray.get(i).getOrderID() == 3 || similarFrame.unitArray.get(i).getOrderID() == 6))
					{
						//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
						
						//System.out.println(gameFrame+"["+unit.getID()+"]����: ����, ���� : �̵� , CurrentCoolDown : "+coolDown+" ���� ���� ��� : "+similarFrame.unitArray.get(i).getNextDirection());
						//System.out.println("���� ���� ������ : "+ similarFrame.currentFrame+" ���� ��ġ [X,Y] : "+unit.getX()+","+unit.getY());
						if(coolDown >= 16 && coolDown <= 18)
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]����: ����, ���� : �̵� , CurrentCoolDown : "+coolDown+" ���� ���� ��� : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("���� ���� ������ : "+ similarFrame.currentFrame+" ���� ��ġ [X,Y] : "+unit.getX()+","+unit.getY());
							if(unit.getHitPoints() + unit.getShields() >= 50.0f && (coolDown >= 16 && coolDown <= 17))
								MoveDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
							if(unit.getHitPoints() + unit.getShields() <= 50.0f)
								MoveDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
						}

						else 
						{
							
						}
						break;
					}
					
					else if((orderID == 3 || orderID == 6) && (similarFrame.unitArray.get(i).getOrderID() == 14 || similarFrame.unitArray.get(i).getOrderID() == 10))
					{

						//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
						//CaptureLine(unit, similarFrame.unitArray.get(i).getNextDirection());
						if(coolDown >= 10){}
						else 
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]����: �̵�, ���� : ���� , CurrentCoolDown : "+coolDown+" ���� ���� ��� : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("���� ���� ������ : "+ similarFrame.currentFrame+" ���� ��ġ [X,Y] : "+unit.getX()+","+unit.getY());
							AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
						}
						break;
					}
					
					else 	
					{

						//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
						if(coolDown >= 24){}
						else
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]����: �̵�, ���� : �̵� , CurrentCoolDown : "+coolDown+" ���� ���� ��� : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("���� ���� ������ : "+ similarFrame.currentFrame+" ���� ��ġ [X,Y] : "+unit.getX()+","+unit.getY());
							
							MoveDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
						}
						break;
					}
				}
			}
		} // for�� ��
		
		return isConform;
	}
	
	public void MoveDirection(Unit unit, int direction)
	{
		//System.out.println("�̵� ����� ���������ϴ� !!!!!!!!!!!!!!!!!!!!");
		
		int value = 150;
		
		int[] numberCount = new int[10];
		int maxNum = 0;
		int maxNumIndex = 0;
		
		/** ������ ���� 9�� ���, �ڱ��� �ֺ� ���� �� ���� ���� ������ ���� �����ϴ�. */
		if(direction == 9)
		{
			
			for(int i = 0; i < numberCount.length; i++)	numberCount[i] = 0; //�ʱ�ȭ
			for(int i = 0; i < similarFrame.unitArray.size(); i++)
			{
				//System.out.println("����� �������� ������"+similarFrame.unitArray.size());
				if(similarFrame.unitArray.get(i).getType() == -1 || similarFrame.unitArray.get(i).getType() == -2) // �Ʊ��̾����
				{
					switch(similarFrame.unitArray.get(i).getNextDirection())
					{
					case 1:	numberCount[1]++;	break;
					case 2:	numberCount[2]++;	break;
					case 3:	numberCount[3]++;	break;
					case 4:	numberCount[4]++;	break;
					case 5:	numberCount[5]++;	break;
					case 6:	numberCount[6]++;	break;
					case 7:	numberCount[7]++;	break;
					case 8:	numberCount[8]++;	break;
					case 9:	numberCount[9]++;	break;
					default: break;
					}
				}
			}

			for(int i = 0; i < numberCount.length; i++)
			{
				if(maxNum <= numberCount[i])
				{
					maxNum = numberCount[i];
					maxNumIndex = i;
				}
				//System.out.println("������ ���� �ִ� ������ : "+numberCount[i]);
			}
			//System.out.println("�ٲ� ���� ������ : "+maxNumIndex);
			direction = maxNumIndex;
		}
		
		
		
		if(direction==1)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX(), unit.getY()-value, 165, false);
			bwapi.move(unit.getID(), unit.getX(), unit.getY()-value); //
		}
		else if(direction==2)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()-value, unit.getY()-value, 165, false);
			bwapi.move(unit.getID(), unit.getX()-value, unit.getY()-value); //
		}
		else if(direction==3)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()-value, unit.getY(), 165, false);
			bwapi.move(unit.getID(), unit.getX()-value, unit.getY()); //
		}
		else if(direction==4)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()-value, unit.getY()+value, 165, false);
			bwapi.move(unit.getID(), unit.getX()-value, unit.getY()+value); //
		}
		else if(direction==5)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX(), unit.getY()+value, 165, false);
			bwapi.move(unit.getID(), unit.getX(), unit.getY()+value); //
		}
		else if(direction==6)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()+value, unit.getY()+value, 165, false);
			bwapi.move(unit.getID(), unit.getX()+value, unit.getY()+value); //
		}
		else if(direction==7)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()+value, unit.getY(), 165, false);
			bwapi.move(unit.getID(), unit.getX()+value, unit.getY()); //
		}
		else if(direction==8)
		{
			//bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()+value, unit.getY()-value, 165, false);
			bwapi.move(unit.getID(), unit.getX()+value, unit.getY()-value); //
		}
		else if(direction==9)
		{
			//bwapi.move(unit.getID(), unit.getX(), unit.getY());
			//System.out.println("�ڱ��ڽ� �̵���� ������..");
		}

	}
	
	public void AttackDirection(Unit unit, int direction)
	{
		int value = 150;
		
		int[] numberCount = new int[10];
		int maxNum = 0;
		int maxNumIndex = 0;
		
		/** ������ ���� 9�� ���, �ڱ��� �ֺ� ���� �� ���� ���� ������ ���� �����ϴ�. */
		if(direction == 9)
		{
			
			for(int i = 0; i < numberCount.length; i++)	numberCount[i] = 0; //�ʱ�ȭ
			for(int i = 0; i < similarFrame.unitArray.size(); i++)
			{
				//System.out.println("����� �������� ������"+similarFrame.unitArray.size());
				if(similarFrame.unitArray.get(i).getType() == -1 || similarFrame.unitArray.get(i).getType() == -2) // �Ʊ��̾����
				{
					switch(similarFrame.unitArray.get(i).getNextDirection())
					{
					case 1:	numberCount[1]++;	break;
					case 2:	numberCount[2]++;	break;
					case 3:	numberCount[3]++;	break;
					case 4:	numberCount[4]++;	break;
					case 5:	numberCount[5]++;	break;
					case 6:	numberCount[6]++;	break;
					case 7:	numberCount[7]++;	break;
					case 8:	numberCount[8]++;	break;
					case 9:	numberCount[9]++;	break;
					default: break;
					}
				}
			}

			for(int i = 0; i < numberCount.length; i++)
			{
				if(maxNum <= numberCount[i])
				{
					maxNum = numberCount[i];
					maxNumIndex = i;
				}
				//System.out.println("������ ���� �ִ� ������ : "+numberCount[i]);
			}
			//System.out.println("�ٲ� ���� ������ : "+maxNumIndex);
			direction = maxNumIndex;
		}
		
		//System.out.println("���� ���� : " + direction);
		if(direction==1)
		{
			bwapi.attack(unit.getID(), unit.getX(), unit.getY()-value); //
		}
		else if(direction==2)
		{
			bwapi.attack(unit.getID(), unit.getX()-value, unit.getY()-value); //
		}
		else if(direction==3)
		{
			bwapi.attack(unit.getID(), unit.getX()-value, unit.getY()); //
		}
		else if(direction==4)
		{
			bwapi.attack(unit.getID(), unit.getX()-value, unit.getY()+value); //
		}
		else if(direction==5)
		{
			bwapi.attack(unit.getID(), unit.getX(), unit.getY()+value); //
		}
		else if(direction==6)
		{
			bwapi.attack(unit.getID(), unit.getX()+value, unit.getY()+value); //
		}
		else if(direction==7)
		{
			bwapi.attack(unit.getID(), unit.getX()+value, unit.getY()); //
		}
		else if(direction==8)
		{
			bwapi.attack(unit.getID(), unit.getX()+value, unit.getY()-value); //
		}
		else if(direction==9)
		{ 	
			
			//if(unit.getTypeID() == UnitTypes.Protoss_Dragoon.getID())
			//	bwapi.holdPosition(unit.getID());
			
			/*int count = 0;
			
			for(Unit u : bwapi.getEnemyUnits())
			{
				if(u.getTypeID() == UnitTypes.Protoss_Zealot.getID() ||  u.getTypeID() == UnitTypes.Protoss_Dragoon.getID())
					count++;
			}
			
			if(count == 0)
				return;
			
			int minInfluence = 0;
			int minInfluence_x  = 0, minInfluence_y = 0;
			for(int j = 0; j < PreProcess.SIZE_Y; j++)
			{
				for(int k = 0; k < PreProcess.SIZE_X; k++)
				{
					if(minInfluence >= currentPlay.get(frameIndex).enLocalInfluenceMap[j][k])
					{
						minInfluence = (int)currentPlay.get(frameIndex).enLocalInfluenceMap[j][k];
						minInfluence_x = k;
						minInfluence_y = j;
					}
				}
			}
			bwapi.attack(unit.getID(), (minInfluence_x+2) * PreProcess.DIVIDE_SIZE, (minInfluence_y+2) * PreProcess.DIVIDE_SIZE);
			*/
			
			//System.out.println("Ÿ�� ���� X, Y ��ǥ : "+ (minInfluence_x) * PreProcess.DIVIDE_SIZE + ", " + (minInfluence_y) * PreProcess.DIVIDE_SIZE);
			
			// - ���� �� ����
			//bwapi.holdPosition(unit.getID());
			//System.out.println("�ڱ��ڽ� ���ݸ�� ������..");
		}

	}
	
	public void CaptureLine(Unit unit, int direction)
	{
		int value = 150;
		if(direction==1)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX(), unit.getY()-value, 111, false);
		}
		else if(direction==2)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()-value, unit.getY()-value, 111, false);
		}
		else if(direction==3)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()-value, unit.getY(), 111, false);
		}
		else if(direction==4)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()-value, unit.getY()+value, 111, false);
		}
		else if(direction==5)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX(), unit.getY()+value, 111, false);
		}
		else if(direction==6)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()+value, unit.getY()+value, 111, false);
		}
		else if(direction==7)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()+value, unit.getY(), 111, false);
		}
		else if(direction==8)
		{
			bwapi.drawLine(unit.getX(), unit.getY(), unit.getX()+value, unit.getY()-value, 111, false);
		}
	}
	
	
	
	@Override
	public void keyPressed(int keyCode) {}
	@Override
	public void matchEnd(boolean winner) 
	{
		FileWriter fw = null;
		RandomAccessFile fw2 = null;
		FileWriter fw3 = null;
		
		try {
			fw = new FileWriter("FileData/Time/Total"+gameNumber+".txt");
			fw2 = new RandomAccessFile("FileData/Winner/WinnerText.txt", "rw");
			fw3 = new FileWriter("FileData/Time/Time"+gameNumber+".txt");
			fw2.seek(fw2.length()); // ���� ��ġ ����
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(int i = 1; i < saveTimer.size(); i++)
		{
			try {
				fw.write(Float.toString(saveTimer.get(i)));
				fw.write(",");
				//if(i <= expandAmount.size())
				fw.write(Float.toString(expandAmount.get(i)));
				fw.write(",");
				fw.write(Integer.toString(hashLevel.get(i)));
				fw.write(",");
				fw.write(Integer.toString(totalReferenceScene.get(i)));
				fw.write(",");
				fw.write(Integer.toString(currentFrameNumber.get(i)));
				fw.write("\r\n");
				
				fw3.write(Float.toString(makeIMTimer.get(i)));
				fw3.write(",");
				fw3.write(Float.toString(compareFrameTimer.get(i)));
				fw3.write(",");
				fw3.write(Float.toString(orderTimer.get(i)));
				fw3.write(",");
				fw3.write(Float.toString(hashTimer.get(i)));
				fw3.write(",");
				fw3.write(Float.toString(saveTimer.get(i)));
				fw3.write(",");
				fw3.write(Integer.toString(currentFrameNumber.get(i)));
				fw3.write("\r\n");
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		try {
			if(winner == true)
			{
				fw2.writeBytes("Win");
				fw2.writeBytes("\r\n");
			}
			else
			{
				fw2.writeBytes("Lose");
				fw2.writeBytes("\r\n");			
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		try {
			fw.close();
			fw2.close();
			fw3.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public String OrderID(int id)
	{
		if(id == 14 || id == 10)
			return "Attack";
		else if(id == 3)
			return "Stop";
		else
			return "Move";
		
	}
	
	public void build(int unit, int unitType, int priorty)
	{
		
	}
	
	@Override
	public void sendText(String text) {}
	@Override
	public void receiveText(String text) {}
	@Override
	public void nukeDetect(int x, int y) {}
	@Override
	public void nukeDetect() {}
	@Override
	public void playerLeft(int playerID) {}
	@Override
	public void unitCreate(int unitID) {}
	@Override
	public void unitDestroy(int unitID) {}
	@Override
	public void unitDiscover(int unitID) {}
	@Override
	public void unitEvade(int unitID) {}
	@Override
	public void unitHide(int unitID) {}
	@Override
	public void unitMorph(int unitID) {}
	@Override
	public void unitShow(int unitID) {}
	@Override
	public void unitRenegade(int unitID) {}
	@Override
	public void saveGame(String gameName) {}
	@Override
	public void unitComplete(int unitID) {}
	@Override
	public void playerDropped(int playerID) {}
}
