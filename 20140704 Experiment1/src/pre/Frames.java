package pre;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import weka.gui.explorer.PreprocessPanel;


public class Frames {

	public int currentFrame; // 현재 프레임 값
	public boolean currentPlay; // 현재 플레이 중인 프레임인지 아닌지.
	
	/** 맵 배열 선언. 크기는 [2040x840]으로 15로 나눌경우 [136x56] = 7616, 20으로 나눌경우 [102x42] = 4284, 40으로 나눌경우 [51x21] = 1071 */
	public float myLocalInfluenceMap[][] = null;
	public float enLocalInfluenceMap[][] = null;
	
	/** 한 프레임마다의 모든 유닛을 unitArray에 저장 */
	public ArrayList<UnitInfo> unitArray = new ArrayList<UnitInfo>();
	
	/** 같은 프레임 참조 안되게 하기 위해 배열형 int 선언. 300번? 이상 다른게 참조되면 배열 초기화..시켜줄 것????*/
	public static ArrayList<Integer> noneReference = new ArrayList<Integer>();
	
	public static int currentReferenceNumber = 0;
	public static float hashTime = 0;
	public static float compareTime = 0;
	public static float makeIMTime = 0;
	
	/** 파일에서 한줄씩 읽어 처리하기 위한 변수*/
	String line;
	
	/** 랜덤 인덱스. 정확도가 0인게 많으면 이 배열에 넣어서 랜덤으로 접근*/
	ArrayList<Integer> randomIndex = new ArrayList<Integer>();
	
	ArrayList<Integer> referenceIndex = new ArrayList<Integer>();
	
	/** 현재 Frames의 맵 데이터와 Playing Map data와의 정확도 비교. 값이 0에 가까울수록 비슷한 것임*/
	public float accuracy = 0;
	
	public float sumMYInfluence = 0;
	public float sumENInfluence = 0;
	public float sumInfluence = 0;
	
	public boolean isCreateIM = false;
	
	public int myAverageUnitX = 0;
	public int myAverageUnitY = 0;
	
	public int enAverageUnitX = 0;
	public int enAverageUnitY = 0;
	
	public int countMyUnit = 0;
	public int countEnUnit = 0;
	
	public long startHashTime, endHashTime, startCompareTime, endCompareTime, startMakeIMTime, endMakeIMTime;
	
	
	public Frames(int _frame, boolean play)
	{
		currentFrame = _frame;
		currentPlay = play;
	}
	
	public void ComputeNumberOfTotalUnit()
	{
		for(int i = 0; i < unitArray.size(); i++)
		{
			if(unitArray.get(i).type == -1 || unitArray.get(i).type == -2)
				countMyUnit++;
			else
				countEnUnit++;
				
		}
	}
	
	/** 한 유닛의 영향력 계산 */
	public void ComputeInfluence(int i, int influenceRange, boolean isEnemy)
	{
		int range = 0;
		float influence = 1;
		for(int j = 0; j <= influenceRange; j++)
		{
			range = j * 4;
			if(j == 0)
			{
				if(isEnemy == false)
					sumMYInfluence += influence * PreProcess.DRAGOON_POWER * HealthValue(unitArray.get(i).type, unitArray.get(i).health);
				else
					sumENInfluence += influence * PreProcess.DRAGOON_POWER * HealthValue(unitArray.get(i).type, unitArray.get(i).health);
			}
			else
			{
				if(isEnemy == false)
					sumMYInfluence +=  influence * range * PreProcess.DRAGOON_POWER * HealthValue(unitArray.get(i).type, unitArray.get(i).health);
				else
					sumENInfluence +=  influence * range * PreProcess.DRAGOON_POWER * HealthValue(unitArray.get(i).type, unitArray.get(i).health);
			}
			influence -= 0.1;
		}
	}
	
	/** 장면의 유닛들의 위치 평균 값 구하기*/
	public void DistanceAverage()
	{
		int mySumX = 0;
		int mySumY = 0;
		int enSumX = 0;
		int enSumY = 0;
		
		for(int i = 0; i < unitArray.size(); i++)
		{
			if(unitArray.get(i).type == -1 || unitArray.get(i).type == -2)
			{
				mySumX += unitArray.get(i).x;
				mySumY += unitArray.get(i).y;
			}
			else if(unitArray.get(i).type == -3 || unitArray.get(i).type == -4)
			{
				enSumX += unitArray.get(i).x;
				enSumY += unitArray.get(i).y;
			}
		}
		
		if(unitArray.size() != 0)
		{
			myAverageUnitX = mySumX / unitArray.size();
			myAverageUnitY = mySumY / unitArray.size();
			
			enAverageUnitX = enSumX / unitArray.size();
			enAverageUnitY = enSumY / unitArray.size();
		}
		
	}
	
	
	/** 영향력 맵의 합 계산*/
	public void SumInfluence()
	{
		sumMYInfluence = 0;
		sumENInfluence = 0;
		sumInfluence = 0;
		
		for(int i = 0; i < unitArray.size(); i++)
		{
			switch(unitArray.get(i).type)
			{
			case -1:
				ComputeInfluence(i, PreProcess.DRAGOON_INFLUENCE_RANGE, false);
				break;
			case -2:
				ComputeInfluence(i, PreProcess.ZEALOT_INFLUENCE_RANGE, false);
				break;
			case -3:
				ComputeInfluence(i, PreProcess.DRAGOON_INFLUENCE_RANGE, true);
				break;
			case -4:
				ComputeInfluence(i, PreProcess.ZEALOT_INFLUENCE_RANGE, true);
				break;
			}
		}
		
		sumInfluence = sumMYInfluence + sumENInfluence;
		
		//System.out.println("영향력 합계 : " + sumInfluence);
	}
	
	/** 리플레이 데이터에서 처리된 프레임들의 영향력맵과, 현재 플레이 프레임의 영향력맵과 비교하여 가장 비슷한 프레임을 찾아냄 */
	public Frames CompareMap(Frames currentFrame)
	{
		referenceIndex.clear(); // 참조 씬 번호 초기화.
		
		if(currentFrame.unitArray.size() == 0)
		{
			//System.out.println("유닛이 0이므로 참고 안함");
			return PreProcess.frameArray.get(0);
		} // 유닛이 0개일땐 참고 안함.
		
		hashTime = 0;
		compareTime = 0;
		makeIMTime = 0;
		
		int NonReferenceNumber = 0;
		int referenceNumber = 0;
		
		randomIndex.clear();
		
		float minAccuracy = 99999999;
		int minAccuracyIndex = 0;
		
		startHashTime = System.currentTimeMillis();
		
		///////////////////////////////////////////////해시테이블을 통한 데이터 찾기 시작 ///////////////////////////////////////////

		for(int i = -(int)PreProcess.differUnitCount; i <= (int)PreProcess.differUnitCount; i++)
		{
			for(int j = -(PreProcess.unitDistancehashValue); j <= PreProcess.unitDistancehashValue; j++)
			{
				for(int k = -(PreProcess.unitDistancehashValue); k <= PreProcess.unitDistancehashValue; k++)
				{
					if(currentFrame.countMyUnit + i < 0 || (currentFrame.myAverageUnitX / 64) + j < 0 || (currentFrame.myAverageUnitX / 64) + j >= 64
							|| (currentFrame.myAverageUnitY / 64) + k < 0 || (currentFrame.myAverageUnitY / 64) + k >= 64)
						continue; // 배열 인덱스 오류 검사
					
					for(int l = 0; l < PreProcess.hashtable[currentFrame.countMyUnit + i]
							[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.size(); l++) // 해시테이블의 사이즈만큼.
					{
						/*if(Math.abs(currentFrame.sumInfluence - 
								PreProcess.frameArray.get(PreProcess.hashtable[currentFrame.countMyUnit + i]
								[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l)).sumInfluence) 
								> PreProcess.hashValue)
							continue; // 영향력 합 필터링
						
						if(Math.abs(currentFrame.enAverageUnitX - PreProcess.frameArray.get(PreProcess.hashtable[currentFrame.countMyUnit + i]
								[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l)).enAverageUnitX) / 64 >= PreProcess.unitDistancehashValue
								|| Math.abs(currentFrame.enAverageUnitY - PreProcess.frameArray.get(PreProcess.hashtable[currentFrame.countMyUnit + i]
										[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l)).enAverageUnitY) / 64 >= PreProcess.unitDistancehashValue)
							continue; // 적 위치 기반 필터링
						*/
						
						referenceIndex.add(PreProcess.hashtable[currentFrame.countMyUnit + i]
								[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l));
						
						referenceNumber++;
						
						if(referenceNumber >= PreProcess.maxReferenceCount)
							break;
					}
					
					if(referenceNumber >= PreProcess.maxReferenceCount)
						break;
				}
				
				if(referenceNumber >= PreProcess.maxReferenceCount)
					break;
			}
			
			if(referenceNumber >= PreProcess.maxReferenceCount)
				break;
			
		}	

		///////////////////////////////////////////////해시테이블을 통한 데이터 찾기 종료 ///////////////////////////////////////////

		///////////////////////////////////////////////데이터 필터 레벨 값 조정 시작 //////////////////////////////////////////////
		
		if(referenceNumber >= PreProcess.maxReferenceCount * 0.7f) // 참조하는 장면이 3000장면이 넘는 경우 참조개수 증가 멈춤.
		{

			//PreProcess.hashValue -= 100;
			//if(PreProcess.hashValue < PreProcess.minHashValue)
			//	PreProcess.hashValue = PreProcess.minHashValue;
			
			PreProcess.unitDistancehashValue -= 1;
			if(PreProcess.unitDistancehashValue < PreProcess.minUnitDistancehashValue)
				PreProcess.unitDistancehashValue = PreProcess.minUnitDistancehashValue;
			
			PreProcess.differUnitCount -= 1;
			if(PreProcess.differUnitCount < PreProcess.minDifferUnitCount)
				PreProcess.differUnitCount = PreProcess.minDifferUnitCount;
		}
	
		else if(referenceNumber <= PreProcess.maxReferenceCount * 0.3f)
		{
			//PreProcess.hashValue += 100;
			//if(PreProcess.hashValue >= PreProcess.maxHashValue)
			//	PreProcess.hashValue = PreProcess.maxHashValue;
			
			PreProcess.unitDistancehashValue += 1;
			if(PreProcess.unitDistancehashValue >= PreProcess.maxUnitDistancehashValue)
				PreProcess.unitDistancehashValue = PreProcess.maxUnitDistancehashValue;
			
			PreProcess.differUnitCount += 1;
			if(PreProcess.differUnitCount >= PreProcess.maxDifferUnitCount)
				PreProcess.differUnitCount = PreProcess.maxDifferUnitCount;
		}
		
		///////////////////////////////////////////////데이터 필터 레벨 값 조정 끝 //////////////////////////////////////////////

		endHashTime = System.currentTimeMillis();
		
		hashTime = (endHashTime - startHashTime);

		currentReferenceNumber = referenceNumber;
		
		startMakeIMTime = System.currentTimeMillis();
		///////////////////////////////////////////////영향력 맵 배열 메모리 할당 및 영향력 맵 생성 시작///////////////////////////////////////////
		for(int i = 0; i < referenceIndex.size(); i++)
		{
			if(PreProcess.frameArray.get(referenceIndex.get(i)).isCreateIM == false)
			{

				PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
				PreProcess.frameArray.get(referenceIndex.get(i)).enLocalInfluenceMap = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
				PreProcess.frameArray.get(referenceIndex.get(i)).InfluenceProcessing();
				PreProcess.frameArray.get(referenceIndex.get(i)).isCreateIM = true;

			}
		}
		endMakeIMTime = System.currentTimeMillis();
		makeIMTime += endMakeIMTime - startMakeIMTime;
		///////////////////////////////////////////////영향력 맵 배열 메모리 할당 및 영향력 맵 생성 끝 //////////////////////////////////////////////
		
		for(int i = 0; i < referenceIndex.size(); i++)
		{	
			PreProcess.frameArray.get(referenceIndex.get(i)).accuracy = 0;
		} // 설정되었던 accuracy 값 초기화.

		startCompareTime = System.currentTimeMillis();
		////////////////////////////////////////////////////////영향력 맵 비교 시작//////////////////////////////////////////////////////////
		for(int i = 0; i < referenceIndex.size(); i++)
		{	

			
			for(int j = 0; j < PreProcess.SIZE_Y; j++)
			{
				for(int k = 0; k < PreProcess.SIZE_X; k++)
				{
					PreProcess.frameArray.get(referenceIndex.get(i)).accuracy += 
							(PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap[j][k] - currentFrame.myLocalInfluenceMap[j][k]) * (PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap[j][k] - currentFrame.myLocalInfluenceMap[j][k]);
					PreProcess.frameArray.get(referenceIndex.get(i)).accuracy += 
							(PreProcess.frameArray.get(referenceIndex.get(i)).enLocalInfluenceMap[j][k] - currentFrame.enLocalInfluenceMap[j][k]) * (PreProcess.frameArray.get(referenceIndex.get(i)).enLocalInfluenceMap[j][k] - currentFrame.enLocalInfluenceMap[j][k]);
					
					// 값의 차이 구해서 전부 더하기. float 와 int이기 때문에 그냥 내림(?)이 처리되므로, 정확도를 높이기 위한 반올림이라든지 작업이 필요.
				}
			}
		}
		endCompareTime = System.currentTimeMillis();
		compareTime += endCompareTime - startCompareTime; 
		///////////////////////////////////////////////////////////영향력 맵 비교 끝/////////////////////////////////////////////////////////

		System.out.println("참조 하는 개수: " + referenceNumber);
		//System.out.println("참조 안하는 개수: " + NonReferenceNumber);
		//System.out.println("총 장면 개수 : "+ PreProcess.lineNumber);

		
		startHashTime = System.currentTimeMillis();
		
		//이부분----------------------------------------------
		for(int i = 0; i < referenceIndex.size(); i++)
		{
			if(PreProcess.frameArray.get(referenceIndex.get(i)).isCreateIM == false)
				continue;
			
			boolean sameFlag = false;
			int sameCount = 0;
			
			boolean referenceFlag = false;
			
			for(int l = 0; l < PreProcess.nonReferenceFrameNumber.size(); l++)
			{
				if(PreProcess.nonReferenceFrameNumber.get(l) == referenceIndex.get(i))
					referenceFlag = true;
			}
			if(referenceFlag == true)
				continue;
			//여기까지 참조되면 안될 프레임이 참조된다면, 건너뛰기. 
			
			for(int j = 0; j < noneReference.size(); j++)
			{
				if(noneReference.get(j) == PreProcess.frameArray.get(referenceIndex.get(i)).currentFrame)
				{
					sameCount++;
					if(sameCount >= 2)
					{
						sameFlag = true;
					}
				}
			} // 한번 참조한 값이 들어오면 중복 되는것을 계속 참조하는 것을 방지하기 위해 true로 바꿈
			
			if(sameFlag == true) // 중복되지 않게끔 건너뜀.
				continue;

			//System.out.println("minAccuracy : " + PreProcess.frameArray.get(i).accuracy);
			
			if(minAccuracy >= PreProcess.frameArray.get(referenceIndex.get(i)).accuracy)
			{
				minAccuracy = PreProcess.frameArray.get(referenceIndex.get(i)).accuracy;
				minAccuracyIndex = referenceIndex.get(i);
				
				// 인덱스와 값을 저장.

				if(minAccuracy == 0)
				{
					randomIndex.add(referenceIndex.get(i));
				}
			}
		} // for문 끝
		
		

		
		System.out.println("minAccuracy 값은 : " + minAccuracy);
		//System.out.println("minAccuracy 주소는 :" + minAccuracyIndex);
		
		if(noneReference.size() <= 200)
		{
			noneReference.add(PreProcess.frameArray.get(minAccuracyIndex).currentFrame);
		}
		else
			noneReference.clear(); // 사이즈가 200이 넘어가면 지금까지 참조했던 것들 전부 초기화..

		
		if(minAccuracy == 0)
		{
			int randomValue = (int) (Math.random() * randomIndex.size());

			//System.out.println("사이즈 : "+randomIndex.size());
			//System.out.println("값 : "+randomValue);
			
			for(int i = 0; i < referenceIndex.size(); i++)
			{
				if(referenceIndex.get(i) == randomIndex.get(randomValue))
					continue; // 이게 같으면 참조할 장면이므로 그건 초기화 하지 않음..
				
				PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap = null; // 참조 안된 장면의 influence map = null
				PreProcess.frameArray.get(referenceIndex.get(i)).enLocalInfluenceMap = null;
				PreProcess.frameArray.get(referenceIndex.get(i)).isCreateIM = false;
			}
			
			endHashTime = System.currentTimeMillis();
			
			hashTime += endHashTime - startHashTime;
			return PreProcess.frameArray.get(randomIndex.get(randomValue));
		}
		else
		{
			for(int i = 0; i < referenceIndex.size(); i++)
			{
				if(referenceIndex.get(i) == minAccuracyIndex)
					continue; // 이게 같으면 참조할 장면이므로 그건 초기화 하지 않음..
				
				PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap = null; // 참조 안된 장면의 influence map = null
				PreProcess.frameArray.get(referenceIndex.get(i)).enLocalInfluenceMap = null;
				PreProcess.frameArray.get(referenceIndex.get(i)).isCreateIM = false;
			}
			endHashTime = System.currentTimeMillis();
			
			hashTime += endHashTime - startHashTime;
			return PreProcess.frameArray.get(minAccuracyIndex);
		}
		// 가장 비슷한 프레임 넘겨주기
	}

	// 32 -1 134 87 180 0 -100 15 -3 1099 397 180 0 -100 0 // String의 예시.
	/** output.txt의 라인 한줄을 읽어 각 유닛들에게 정보를 넣어주기*/
	public void createUnit()
	{
		String[] splitLine = line.split("-100"); // -100단위로 쪼갬. 즉 유닛 단위로 쪼갬

		for(int i = 0; i < splitLine.length; i++)
		{
			String[] splitSpace = splitLine[i].split(" "); // 스페이스 바로 구분
			
			if(splitSpace.length <= 2)
			{
				break;
			} // 마지막 프레임 쪽이면, 값이 없으므로 종료.
			
			if(Integer.parseInt(splitSpace[1]) != 999999) // 0이면 ID가 없으므로 유닛이 없는 것.
			{			
				int id, type, x, y, health, orderID;
				
				id = Integer.parseInt(splitSpace[1]);
				type = Integer.parseInt(splitSpace[2]);
				x = Integer.parseInt(splitSpace[3]);
				y = Integer.parseInt(splitSpace[4]);
				health = Integer.parseInt(splitSpace[5]);
				orderID = Integer.parseInt(splitSpace[6]);
				// 정보들을 쪼개어 각 변수들에 저장.

				if(type >= -4) // 임시 코드
					unitArray.add(new UnitInfo(id, type, x, y, health, orderID, 0, 0, false)); // 유닛 추가.
				
				//System.out.println("id : "+unitArray.get(count).id+" type : "+unitArray.get(count).type+" x : "+unitArray.get(count).x+" y : "+unitArray.get(count).y);	
			}
		}
	}

	/** 저장된 정보들로 전처리를 하여 다음이동 방향 과 현재의 위치 체크 */
	public void DataProcessing(Frames nextFrame) // 파라미터 값이 있는 이유는, 현재 Frames의 다음 값을 참조하기 위함.
	{
		/** UnitArray만큼 돌아서 현재 변환된 맵의 위치, 다음 이동방향 등 체크하기*/
		for(int i = 0; i < unitArray.size(); i++)
		{
			setPosition(); // 포지션 값 세팅
			
			for(int j = 0; j < nextFrame.unitArray.size(); j++) // 이중 for문, 이용하여 2개의 유닛리스트들 비교하여 ID값 찾아내고 계산
			{
				if(unitArray.get(i).id == nextFrame.unitArray.get(j).id)
				{
					boolean zeroCheck = false;
					
					double rad = Math.atan2(nextFrame.unitArray.get(j).x - unitArray.get(i).x, nextFrame.unitArray.get(j).y - unitArray.get(i).y); // 방향 계산 식
					if(nextFrame.unitArray.get(j).x == unitArray.get(i).x && nextFrame.unitArray.get(j).y == unitArray.get(i).y)
					{
						zeroCheck = true;
					} // 이부분은 실제로 유닛이 하나도 안움직였을 시 5번 방향으로 가기 때문에, 그것을 체크해줘서 5번으로 안가게끔 하기 위함.
					double degree = (rad * 180.0)/Math.PI; // 방향 계산 식
					unitArray.get(i).nextDirection = ConvertDegree(degree, zeroCheck); // 현재 유닛의 다음 방향 저장
					
					break;
				}
			}
		}
	}
	
	public void setPosition()
	{
		for(int i = 0; i < unitArray.size(); i++)
		{
			int x, y;
			x = unitArray.get(i).x / PreProcess.DIVIDE_SIZE;
			y = unitArray.get(i).y / PreProcess.DIVIDE_SIZE;
			
			unitArray.get(i).position = x + y * PreProcess.SIZE_X; // 포지션 구하는 식
		}
	}
	
	/** 영향력 값 측정*/
	public void InfluenceProcessing()
	{
		for(int i = 0; i < unitArray.size(); i++)
		{
			int y = unitArray.get(i).position % PreProcess.SIZE_X;
			int x = unitArray.get(i).position / PreProcess.SIZE_X;
			// position을 2차원 배열값으로 바꿈
			
			switch(unitArray.get(i).type)
			{
			case -1:
				DiamondInfluence(i, x, y, PreProcess.DRAGOON_INFLUENCE_RANGE, false);
				break; // 아군 드라군
			case -2:
				DiamondInfluence(i, x, y, PreProcess.ZEALOT_INFLUENCE_RANGE, false);
				break; // 아군 질럿
			case -3:
				DiamondInfluence(i, x, y, PreProcess.DRAGOON_INFLUENCE_RANGE, true);
				break; // 적군 드라군
			case -4:
				DiamondInfluence(i, x, y, PreProcess.ZEALOT_INFLUENCE_RANGE, true);
				break; // 적군 질럿
			default:
				break;
			}
		}
	}
	
	/**영향력 맵 만들기. currentIndex는 현재의 유닛 Array 번호. x와 y는 배열상의 좌표. influence는 영향력(파워), enemy는 적인지 아닌지 판단 */
	public void DiamondInfluence(int currentIndex,int x, int y, int influence, boolean enemy)
	{
		for(int i = x - influence; i < x + influence; i++)
		{
			for(int j = y - influence; j < y + influence; j++) // 총 -influence부터 + influence까지 찾고, 칸수차이가 4이상 나는것은 아직 미처리
			{
				if(i < 0 || j < 0 || j >= PreProcess.SIZE_X || i >= PreProcess.SIZE_Y) // 인덱스 에러 찾기
					continue;

				switch(Math.abs(x - i) + Math.abs(y - j)) // 현재 유닛 좌표와 맵 좌표간의 거리 차이. 좌표점 수 차이..
				{
				// 현재는 드라군파워로 질럿도 공용으로 사용 중.
				case 0:
					if(enemy == false)
						myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
						* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 1.0;
					else
						enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
						* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 1.0;
					break;
				case 1:
					if(enemy == false)
						myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
						* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.9;
					else
						enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
						* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.9;
					break;
				case 2:
					if(enemy == false)
						myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
						* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.8;
					else
						enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
						* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.8;
					break;
				case 3:
					if(influence >= 3)
					{
						if(enemy == false)
							myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.7;
						else
							enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.7;
					}
					break;
				case 4:
					if(influence >= 4)
					{
						if(enemy == false)
							myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.6;
						else
							enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.6;
					}
					break;
				case 5:
					if(influence >= 5)
					{
						if(enemy == false)
							myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.5;
						else
							enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.5;
					}
					break;
				case 6:
					if(influence >= 6)
					{
						if(enemy == false)
							myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.4;
						else
							enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.4;
					}
					break;
				case 7:
					if(influence >= 7)
					{
						if(enemy == false)
							myLocalInfluenceMap[i][j] += (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.3;
						else
							enLocalInfluenceMap[i][j] -= (double)PreProcess.DRAGOON_POWER 
							* HealthValue(unitArray.get(currentIndex).type, unitArray.get(currentIndex).health) * 0.3;
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	/** 영향력 맵 확장. 즉 해상도를 낮춤*/
	public void ExpandInfluence(int level)
	{
		for(int i = 0; i < unitArray.size(); i++)
		{
			//우선 유닛이 카피된 유닛인지 아닌지 판단.
			if(unitArray.get(i).copyUnit == false)
			{
				for(int j = unitArray.get(i).x - level * PreProcess.DIVIDE_SIZE; j <= unitArray.get(i).x + level * PreProcess.DIVIDE_SIZE; j += PreProcess.DIVIDE_SIZE)
				{
					for(int k = unitArray.get(i).y - level * PreProcess.DIVIDE_SIZE; k <= unitArray.get(i).y + level * PreProcess.DIVIDE_SIZE; k += PreProcess.DIVIDE_SIZE)
					{
						if(j < 0 || k < 0)
							continue; // x,y좌표 오류범위 체크
						if(unitArray.get(i).type == -1 || unitArray.get(i).type == -2) // 아군 유닛 이어야 객체 생성
						{
							unitArray.add(new UnitInfo(unitArray.get(i).id, unitArray.get(i).type, j, k, unitArray.get(i).health,
									unitArray.get(i).orderID, 0, unitArray.get(i).nextDirection, true));
							//System.out.println("객체가 생성되었습니다");
						}
					}
				} //원형으로 방향 복제
			}
		}
		setPosition(); // 생성된 유닛들의 포지션 설정
	}
	
	public double HealthValue(int type, int currentHealth)
	{
		double value = 1;
		
		if(type == -1 || type == -3) // 드라군이면
		{
			//value = (double)currentHealth / (double)PreProcess.DRAGOON_HEALTH;
			if((double)currentHealth / (double)PreProcess.DRAGOON_HEALTH >= 0.5)
			{
				value = 1;
			} // 현재 체력이 50% ~ 100%이면 곱하는 값 1
			else if((double)currentHealth / (double)PreProcess.DRAGOON_HEALTH >= 0.3)
			{
				value = 0.7;
			} // 현재 체력이 30% ~ 50%이면 곱하는 값 0.7
			else
			{
				value = 0.5;
			} // 현재 체력이 0% ~ 30%이면 곱하는 값 0.5
			
		}
		else if(type == -2 || type == -4) // 질럿이면
		{
			//value = (double)currentHealth / (double)PreProcess.ZEALOT_HEALTH;
			if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.6)
			{
				value = 1;
			} // 현재 체력이 80% ~ 100%이면 곱하는 값 1
			else if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.3)
			{
				value = 0.7;
			} // 현재 체력이 50% ~ 80% 이면 곱하는 값 0.6
			else if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.2)
			{
				value = 0.5;
			} // 현재 체력이 30% ~ 50% 이면 곱하는 값 0.4
			else if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.1)
			{
				value = 0.35;
			} // 현재 체력이 30% ~ 50% 이면 곱하는 값 0.4
			else
			{
				value = 0.2;
			}// 현재 체력이 0% ~ 20% 이면 곱하는 값 0.2
			
		}
		
		return value;
	}
	
	
	public void WriteFile(int part) throws IOException
	{
		RandomAccessFile out = new RandomAccessFile(PreProcess.address, "rw");
		out.seek(out.length()); // 파일 위치 참조
		
		if(part == 0) // 현재위치와 다음방향 저장
		{
			for(int i = 0; i < unitArray.size(); i++)
			{
				out.writeBytes(Integer.toString(unitArray.get(i).id));
				out.writeBytes(" ");
				out.writeBytes(Integer.toString(unitArray.get(i).position));
				out.writeBytes(" ");
				out.writeBytes(Integer.toString(unitArray.get(i).orderID));
				out.writeBytes(" ");
				out.writeBytes(Integer.toString(unitArray.get(i).nextDirection));
				out.writeBytes(" ");
				out.writeBytes("-100");
				out.writeBytes(" ");
			} // id, 현재위치, 어택상태, 다음방향 저장 
			
			out.writeBytes("\r\n"); // 한줄 띄우기
		}
		
		else if(part == 1) // 영향력 맵 저장
		{
			out.writeBytes("\r\n");
			out.writeBytes("--Influence Map--");
			out.writeBytes("\r\n");
			out.writeBytes("\r\n");
			
			for(int i = 0; i < PreProcess.SIZE_Y; i++)
			{
				for(int j = 0; j < PreProcess.SIZE_X; j++)
				{
					out.writeBytes(Integer.toString((int)myLocalInfluenceMap[i][j]));
					out.writeBytes(",");
				}
				out.writeBytes("\r\n");
			}
			
			out.writeBytes("\r\n");
			out.writeBytes("--Influence2 Map--");
			out.writeBytes("\r\n");
			out.writeBytes("\r\n");
			
			for(int i = 0; i < PreProcess.SIZE_Y; i++)
			{
				for(int j = 0; j < PreProcess.SIZE_X; j++)
				{
					out.writeBytes(Integer.toString((int)enLocalInfluenceMap[i][j]));
					out.writeBytes(",");
				}
				out.writeBytes("\r\n");
			}
		}
		out.close();
	}
	
	// 1 -> 북쪽 7-> 동쪽 3-> 서쪽 5-> 남쪽 0-> 움직임 없음?
	/** 스타크래프트 에 맞는 각도 체크하여 방향 설정.*/
	public int ConvertDegree(double degree, boolean check)
	{
		int direction = 0;
		
		if(check == true)
		{
			direction = 9;
			return direction;
		}
		if(degree < -157.5 || degree > 157.5)
			direction = 1;
		else if(degree <= -90 - 22.5)
			direction = 2;
		else if(degree <= -45 - 22.5)
			direction = 3;
		else if(degree <= 0 - 22.5)
			direction = 4;
		else if(degree <= 45 - 22.5)
			direction = 5;
		else if(degree <= 90 - 22.5)
			direction = 6;
		else if(degree <= 135 - 22.5)
			direction = 7;
		else if(degree <= 180 - 22.5)
			direction = 8;	
		
		return direction;
	}
	
	
	
	
}
