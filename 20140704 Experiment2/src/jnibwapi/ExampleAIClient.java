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

//실험2

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
	
	//------------------인석의 추가 코드----------------------//
	
	/** 현재 진행되는 게임내의 유닛들에 대한 정보를 담음.*/ 
	ArrayList<Frames> currentPlay;
	
	/** 리턴된 영향력 맵이 가장 일치하는 프레임 */
	Frames similarFrame;
	
	/** 현재 프레임 찾기*/
	int frameIndex;
	
	int similarFrameIndex;
	
	/** 현재 게임 프레임*/
	private int gameFrame;
	
	private int startFrame;
	private boolean isStartFrame = false;
	
	/** 게임 프레임 조정*/
	private final int GAMEFRAMECOUNT = 3;
	
	/** 최대 해상도 낮춤 수..*/
	private final int EXPAND_LEVEL_MAX = 5;
	
	/** 시간 측정*/
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

		/** Insert Code 인석*/
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
			//	PreProcess.LIMITLINE = 200000; // 게임 씬 장면 자동화.
			//if(gameNumber == 200)
			//	PreProcess.LIMITLINE = 50000;		
			//if(gameNumber == 400)
			//	PreProcess.LIMITLINE = 25000;
			//if(gameNumber == 600)
			//	PreProcess.LIMITLINE = 25000;
			
			/*if(gameNumber == 40)
				PreProcess.maxReferenceCount = 125; // 참조 개수 자동화.
			if(gameNumber == 80)
				PreProcess.maxReferenceCount = 250; // 참조 개수 자동화.
			if(gameNumber == 120)
				PreProcess.maxReferenceCount = 500; // 참조 개수 자동화.
			if(gameNumber == 160)
				PreProcess.maxReferenceCount = 2000; // 참조 개수 자동화.*/
			
			
			firstStart = true;
			
			Frames.noneReference = null;
			Frames.noneReference = new ArrayList<Integer>();
			
			
			PreProcess.frameArray = null;
			PreProcess.frameArray = new ArrayList<Frames>(); // 메모리 다시 할당..
			
			PreProcess.hashtable = null;
			
			PreProcess.hashtable = new HashTable[100][64][64];
			
			for(int i = 0; i < 100; i++){
				for(int j = 0; j < 64; j++){
					for(int k = 0; k < 64; k++)
					{
						PreProcess.hashtable[i][j][k] = new HashTable();
					}
				}
			} // hashtable 초기화
			
			PreProcess.nonReferenceFrameNumber = null;
			PreProcess.nonReferenceFrameNumber = new ArrayList<Integer>();
			
			PreProcess.totalMyLocalInfluenceMap = null;
			PreProcess.totalEnLocalInfluenceMap = null;
			
			PreProcess.totalMyLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			PreProcess.totalEnLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			
			PreProcess.lineNumber = 0;
			
			System.out.println("데이터 셋 재설정 합니다. 현재 게임 횟수 : "+gameNumber);

		} // 게임 넘버 40일때 빌드를 다시 시작.
		
		
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

		/////////////////////////// 빌드 추가  ///////////////////////////
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
						System.out.println("유닛 타겟 좌표 : "+unit.getTargetX()+","+ unit.getTargetY());
						System.out.println("어택했음");
						break;
					}
					else if(!unit.isCarryingGas())
					{
						bwapi.attack(unit.getID(), 101, 101);
						System.out.println("유닛 타겟 좌표 : "+unit.getTargetX()+","+ unit.getTargetY());
						System.out.println("어택했음");
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
		System.out.println("gameFrame 값은 : "+gameFrame);
		
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
		//////////////////////////여기까지는 화면에 출력에 관한 부분////////////////////////////
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
			
			PreProcessAllUnit(); // 아군과 적군 유닛의 전처리. 리스트에 정보 전처리하여 추가시킴
			currentPlay.get(frameIndex).setPosition(); // 포지션 값 전 처리
			currentPlay.get(frameIndex).myLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			currentPlay.get(frameIndex).enLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
			currentPlay.get(frameIndex).SumInfluence(); // 영향력 맵의 합 계산
			currentPlay.get(frameIndex).ComputeNumberOfTotalUnit(); // 적군 아군 유닛 합 계산
			currentPlay.get(frameIndex).InfluenceProcessing(); // 영향력 맵 세팅
			currentPlay.get(frameIndex).DistanceAverage();
			
			
			/*try {
				currentPlay.get(frameIndex).WriteFile(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			// 비슷한 영향력 맵 찾아 프레임 반환
			similarFrame = currentPlay.get(frameIndex).CompareMap(currentPlay.get(frameIndex));
			similarFrameIndex = similarFrame.currentFrame;
			
			
			System.out.println("비슷한장면 값 : "+similarFrame.sumInfluence+"  현재장면 값 : " +currentPlay.get(frameIndex).sumInfluence);
			//System.out.println("비슷한장면 아군 값 : "+similarFrame.sumMYInfluence+"  현재장면 아군 값 : " +currentPlay.get(frameIndex).sumMYInfluence);
			//System.out.println("비슷한장면 적군 값 : "+similarFrame.sumENInfluence+"  현재장면 적군 값 : " +currentPlay.get(frameIndex).sumENInfluence);
			
			///////////////////////
			//리턴받은 장면과 현재 장면의 유닛들 파일에 작성하기
			
			///////////////////////
			
			//System.out.println("적군 영향력 합 :"+currentPlay.get(frameIndex).sumENInfluence);
			
			/////////////////////
			/** 유닛 움직임 따라하기*/
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
					
					position = temp_X + temp_Y * PreProcess.SIZE_X; // 포지션 구하는 식
					
					boolean isConform = DecideCommander(unit, position, orderID, coolDown, x, y, printCount);
					int expandLevel = 1;
					
					while(isConform == false)
					{
						//영향력 맵 해상도 낮춤. 그리고 한번 더 명령 결정
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
							//System.out.println("Expand 되었음");
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
						} // copy된 객체 삭제
					}
					//true 객체 소멸 필요 copy
				} // if 문 끝
			} // for 문 끝
			
			
			System.out.println("sumExpand : "+sumExpand+" sumMyUnit :"+sumMyUnit);
			
			
			if(sumExpand >= sumMyUnit * 0.7f)
			{
				//System.out.println("모두 expand");
				
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
									bwapi.attack(unit.getID(), 2100, 130); //언덕위
									//System.out.println("X, Y :"+unit.getOr+","+unit.getTargetY());
								}
								else if(countNexus >= 2)*/
									bwapi.attack(unit.getID(), 2000, 2000); //앞마당
								//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
								//bwapi.attack(unit.getID(), (bwapi.getMap().getWalkWidth() * 32) / 2, (bwapi.getMap().getHeight() * 32) / 2);
								System.out.println("모두 expand되어 센터 공격");
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
			// 해쉬 벨류 수정. 유닛 수에 비례하여.. 알맞은 값을 계속 찾음. 속도를 최적화 하기 위해.
			
			//System.out.println("현재의 아군 유닛 수 : " + sumMyUnit);
			System.out.println("현재의 FilterLevel: " + (PreProcess.unitDistancehashValue - PreProcess.minUnitDistancehashValue));
			//System.out.println("현재의 확장 수 : " + sumExpand);
			//System.out.println("sumExpand를 아군 총 유닛으로 나눈 값은 : " + (double)sumExpand / (double)sumMyUnit);
			
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

			/** Test용 파일에 써서 Data 확인하기*/
			
			/*try {
				currentPlay.get(frameIndex).WriteFile(0);
				currentPlay.get(frameIndex).WriteFile(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
		//} // 게임 프레임
		
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
		} // 아군 유닛 처리
		
		for(int i = 0; i < bwapi.getEnemyUnits().size(); i++)
		{
			if(bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Dragoon.getID() 
					|| bwapi.getEnemyUnits().get(i).getTypeID() == UnitTypes.Protoss_Zealot.getID())
			{
				if(bwapi.getEnemyUnits().get(i).isCompleted() == false)
					continue;
				
				//if(bwapi.getEnemyUnits().get(i).isVisible() == false)
				//	continue;
				// 시야에서 안보이면 그 유닛은 체크하지 않음.
				
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
		} // 적군 유닛 처리
	}
	
	// 명령결정 부분
	public boolean DecideCommander(Unit unit, int position, int orderID, int coolDown, int x, int y, int printCount){
		
		boolean isConform = false;
		
		for(int i = 0; i < similarFrame.unitArray.size(); i++)
		{
			// 현재 유닛의 포지션과 가장 비슷한 프레임의 유닛 중 포지션이 같은것이 있으면
			if(similarFrame.unitArray.get(i).getType() == -1 || similarFrame.unitArray.get(i).getType() == -2)
			{
				if(similarFrame.unitArray.get(i).getPosition() == position)
				{

					isConform = true;
					// 현재 유닛을 포지션 같은것을 참고하여 다음 방향으로 이동시킴.
					// 이동 공식
					// 1. 현재 : 공격 , 다음 : 공격 -> 상태변경 X
					// 2. 현재 : 공격 , 다음 : 이동 -> 쿨다운 10 ↑ 이동, 쿨다운 10 ↓ 상태변경 X
					// 3. 현재 : 이동 , 다음 : 공격 -> 쿨다운 10 ↑ 상태변경 X, 쿨다운 10 ↓ 공격( - 높은 곳 타게팅 ? )
					// 4. 현재 : 이동 , 다음 : 이동 -> 방향대로 이동
					
					if(similarFrame.unitArray.get(i).getOrderID() == 107) // order가 Hold면
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
							//System.out.println(gameFrame+"["+unit.getID()+"]현재: 공격, 다음 : 공격 , CurrentCoolDown : "+coolDown+" 다음 방향 모방 : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("현재 참조 프레임 : "+ similarFrame.currentFrame +" 현재 위치 [X,Y] : "+unit.getX()+","+unit.getY());
							
							AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
						}
						else if(unit.isAttacking() == false && ((gameFrame - startFrame) % 24 == 0) && coolDown == 0)
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]현재: 공격, 다음 : 공격 , CurrentCoolDown : "+coolDown+" 다음 방향 모방 : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("현재 참조 프레임 : "+ similarFrame.currentFrame +" 현재 위치 [X,Y] : "+unit.getX()+","+unit.getY());
							
							boolean tooClose = false;
							
							for(Unit enemy: bwapi.getEnemyUnits())
							{
								if(250 > Math.sqrt(Math.pow((unit.getX() - enemy.getX()), 2) + Math.pow((unit.getY() - enemy.getY()), 2)))
								{
									tooClose = true;
									break;
								}
							} // 너무 적 유닛이 가까우면 , 공격명령을 중복적으로 내리지 않기 위해 거리를 재서(드라군의 사거리업 상태의 사거리는 230~240정도) 250보다 작으면 공격명령을 내리지 않음.
							
							if(tooClose == false)
							{
								//System.out.println("적 유닛이 근처에 없어 공격명령을 다시 내림");
								AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
							}
							
						} // 현재 상태가 어택 중이 아니면, 좌표와 방향을 다시 잡아야 하므로, 어택 방향을 잡음. 근데 계속 쿨타임은 0이어서, 어택을 위로 찍거나 반복될 수 있으므로 9프레임 마다 실행되게끔 함.
						
						break;
						//if(coolDown == 0)
						//	AttackDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
					}
					
					else if((orderID == 14 || orderID == 10) && (similarFrame.unitArray.get(i).getOrderID() == 3 || similarFrame.unitArray.get(i).getOrderID() == 6))
					{
						//bwapi.drawText(unit.getX(), unit.getY(), ""+OrderID(unit.getOrderID()), false);
						
						//System.out.println(gameFrame+"["+unit.getID()+"]현재: 공격, 다음 : 이동 , CurrentCoolDown : "+coolDown+" 다음 방향 모방 : "+similarFrame.unitArray.get(i).getNextDirection());
						//System.out.println("현재 참조 프레임 : "+ similarFrame.currentFrame+" 현재 위치 [X,Y] : "+unit.getX()+","+unit.getY());
						if(coolDown >= 16 && coolDown <= 18)
						{
							//System.out.println(gameFrame+"["+unit.getID()+"]현재: 공격, 다음 : 이동 , CurrentCoolDown : "+coolDown+" 다음 방향 모방 : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("현재 참조 프레임 : "+ similarFrame.currentFrame+" 현재 위치 [X,Y] : "+unit.getX()+","+unit.getY());
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
							//System.out.println(gameFrame+"["+unit.getID()+"]현재: 이동, 다음 : 공격 , CurrentCoolDown : "+coolDown+" 다음 방향 모방 : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("현재 참조 프레임 : "+ similarFrame.currentFrame+" 현재 위치 [X,Y] : "+unit.getX()+","+unit.getY());
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
							//System.out.println(gameFrame+"["+unit.getID()+"]현재: 이동, 다음 : 이동 , CurrentCoolDown : "+coolDown+" 다음 방향 모방 : "+similarFrame.unitArray.get(i).getNextDirection());
							//System.out.println("현재 참조 프레임 : "+ similarFrame.currentFrame+" 현재 위치 [X,Y] : "+unit.getX()+","+unit.getY());
							
							MoveDirection(unit, similarFrame.unitArray.get(i).getNextDirection());
						}
						break;
					}
				}
			}
		} // for문 끝
		
		return isConform;
	}
	
	public void MoveDirection(Unit unit, int direction)
	{
		//System.out.println("이동 명령이 떨어졌습니다 !!!!!!!!!!!!!!!!!!!!");
		
		int value = 150;
		
		int[] numberCount = new int[10];
		int maxNum = 0;
		int maxNumIndex = 0;
		
		/** 방향의 값이 9일 경우, 자기의 주변 유닛 중 가장 많은 방향의 값을 따릅니다. */
		if(direction == 9)
		{
			
			for(int i = 0; i < numberCount.length; i++)	numberCount[i] = 0; //초기화
			for(int i = 0; i < similarFrame.unitArray.size(); i++)
			{
				//System.out.println("비슷한 프레임의 사이즈"+similarFrame.unitArray.size());
				if(similarFrame.unitArray.get(i).getType() == -1 || similarFrame.unitArray.get(i).getType() == -2) // 아군이어야함
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
				//System.out.println("현재의 방향 최대 개수는 : "+numberCount[i]);
			}
			//System.out.println("바뀐 공격 방향은 : "+maxNumIndex);
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
			//System.out.println("자기자신 이동명령 떨어짐..");
		}

	}
	
	public void AttackDirection(Unit unit, int direction)
	{
		int value = 150;
		
		int[] numberCount = new int[10];
		int maxNum = 0;
		int maxNumIndex = 0;
		
		/** 방향의 값이 9일 경우, 자기의 주변 유닛 중 가장 많은 방향의 값을 따릅니다. */
		if(direction == 9)
		{
			
			for(int i = 0; i < numberCount.length; i++)	numberCount[i] = 0; //초기화
			for(int i = 0; i < similarFrame.unitArray.size(); i++)
			{
				//System.out.println("비슷한 프레임의 사이즈"+similarFrame.unitArray.size());
				if(similarFrame.unitArray.get(i).getType() == -1 || similarFrame.unitArray.get(i).getType() == -2) // 아군이어야함
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
				//System.out.println("현재의 방향 최대 개수는 : "+numberCount[i]);
			}
			//System.out.println("바뀐 공격 방향은 : "+maxNumIndex);
			direction = maxNumIndex;
		}
		
		//System.out.println("공격 방향 : " + direction);
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
			
			//System.out.println("타겟 지점 X, Y 좌표 : "+ (minInfluence_x) * PreProcess.DIVIDE_SIZE + ", " + (minInfluence_y) * PreProcess.DIVIDE_SIZE);
			
			// - 높은 곳 공격
			//bwapi.holdPosition(unit.getID());
			//System.out.println("자기자신 공격명령 떨어짐..");
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
			fw2.seek(fw2.length()); // 파일 위치 참조
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
