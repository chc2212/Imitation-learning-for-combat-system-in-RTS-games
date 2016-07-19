package pre;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import weka.gui.explorer.PreprocessPanel;


public class Frames {

	public int currentFrame; // ���� ������ ��
	public boolean currentPlay; // ���� �÷��� ���� ���������� �ƴ���.
	
	/** �� �迭 ����. ũ��� [2040x840]���� 15�� ������� [136x56] = 7616, 20���� ������� [102x42] = 4284, 40���� ������� [51x21] = 1071 */
	public float myLocalInfluenceMap[][] = null;
	public float enLocalInfluenceMap[][] = null;
	
	/** �� �����Ӹ����� ��� ������ unitArray�� ���� */
	public ArrayList<UnitInfo> unitArray = new ArrayList<UnitInfo>();
	
	/** ���� ������ ���� �ȵǰ� �ϱ� ���� �迭�� int ����. 300��? �̻� �ٸ��� �����Ǹ� �迭 �ʱ�ȭ..������ ��????*/
	public static ArrayList<Integer> noneReference = new ArrayList<Integer>();
	
	public static int currentReferenceNumber = 0;
	public static float hashTime = 0;
	public static float compareTime = 0;
	public static float makeIMTime = 0;
	
	/** ���Ͽ��� ���پ� �о� ó���ϱ� ���� ����*/
	String line;
	
	/** ���� �ε���. ��Ȯ���� 0�ΰ� ������ �� �迭�� �־ �������� ����*/
	ArrayList<Integer> randomIndex = new ArrayList<Integer>();
	
	ArrayList<Integer> referenceIndex = new ArrayList<Integer>();
	
	/** ���� Frames�� �� �����Ϳ� Playing Map data���� ��Ȯ�� ��. ���� 0�� �������� ����� ����*/
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
	
	/** �� ������ ����� ��� */
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
	
	/** ����� ���ֵ��� ��ġ ��� �� ���ϱ�*/
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
	
	
	/** ����� ���� �� ���*/
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
		
		//System.out.println("����� �հ� : " + sumInfluence);
	}
	
	/** ���÷��� �����Ϳ��� ó���� �����ӵ��� ����¸ʰ�, ���� �÷��� �������� ����¸ʰ� ���Ͽ� ���� ����� �������� ã�Ƴ� */
	public Frames CompareMap(Frames currentFrame)
	{
		referenceIndex.clear(); // ���� �� ��ȣ �ʱ�ȭ.
		
		if(currentFrame.unitArray.size() == 0)
		{
			//System.out.println("������ 0�̹Ƿ� ���� ����");
			return PreProcess.frameArray.get(0);
		} // ������ 0���϶� ���� ����.
		
		hashTime = 0;
		compareTime = 0;
		makeIMTime = 0;
		
		int NonReferenceNumber = 0;
		int referenceNumber = 0;
		
		randomIndex.clear();
		
		float minAccuracy = 99999999;
		int minAccuracyIndex = 0;
		
		startHashTime = System.currentTimeMillis();
		
		///////////////////////////////////////////////�ؽ����̺��� ���� ������ ã�� ���� ///////////////////////////////////////////

		for(int i = -(int)PreProcess.differUnitCount; i <= (int)PreProcess.differUnitCount; i++)
		{
			for(int j = -(PreProcess.unitDistancehashValue); j <= PreProcess.unitDistancehashValue; j++)
			{
				for(int k = -(PreProcess.unitDistancehashValue); k <= PreProcess.unitDistancehashValue; k++)
				{
					if(currentFrame.countMyUnit + i < 0 || (currentFrame.myAverageUnitX / 64) + j < 0 || (currentFrame.myAverageUnitX / 64) + j >= 64
							|| (currentFrame.myAverageUnitY / 64) + k < 0 || (currentFrame.myAverageUnitY / 64) + k >= 64)
						continue; // �迭 �ε��� ���� �˻�
					
					for(int l = 0; l < PreProcess.hashtable[currentFrame.countMyUnit + i]
							[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.size(); l++) // �ؽ����̺��� �����ŭ.
					{
						/*if(Math.abs(currentFrame.sumInfluence - 
								PreProcess.frameArray.get(PreProcess.hashtable[currentFrame.countMyUnit + i]
								[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l)).sumInfluence) 
								> PreProcess.hashValue)
							continue; // ����� �� ���͸�
						
						if(Math.abs(currentFrame.enAverageUnitX - PreProcess.frameArray.get(PreProcess.hashtable[currentFrame.countMyUnit + i]
								[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l)).enAverageUnitX) / 64 >= PreProcess.unitDistancehashValue
								|| Math.abs(currentFrame.enAverageUnitY - PreProcess.frameArray.get(PreProcess.hashtable[currentFrame.countMyUnit + i]
										[(currentFrame.myAverageUnitX / 64) + j][(currentFrame.myAverageUnitY / 64) + k].table.get(l)).enAverageUnitY) / 64 >= PreProcess.unitDistancehashValue)
							continue; // �� ��ġ ��� ���͸�
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

		///////////////////////////////////////////////�ؽ����̺��� ���� ������ ã�� ���� ///////////////////////////////////////////

		///////////////////////////////////////////////������ ���� ���� �� ���� ���� //////////////////////////////////////////////
		
		if(referenceNumber >= PreProcess.maxReferenceCount * 0.7f) // �����ϴ� ����� 3000����� �Ѵ� ��� �������� ���� ����.
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
		
		///////////////////////////////////////////////������ ���� ���� �� ���� �� //////////////////////////////////////////////

		endHashTime = System.currentTimeMillis();
		
		hashTime = (endHashTime - startHashTime);

		currentReferenceNumber = referenceNumber;
		
		startMakeIMTime = System.currentTimeMillis();
		///////////////////////////////////////////////����� �� �迭 �޸� �Ҵ� �� ����� �� ���� ����///////////////////////////////////////////
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
		///////////////////////////////////////////////����� �� �迭 �޸� �Ҵ� �� ����� �� ���� �� //////////////////////////////////////////////
		
		for(int i = 0; i < referenceIndex.size(); i++)
		{	
			PreProcess.frameArray.get(referenceIndex.get(i)).accuracy = 0;
		} // �����Ǿ��� accuracy �� �ʱ�ȭ.

		startCompareTime = System.currentTimeMillis();
		////////////////////////////////////////////////////////����� �� �� ����//////////////////////////////////////////////////////////
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
					
					// ���� ���� ���ؼ� ���� ���ϱ�. float �� int�̱� ������ �׳� ����(?)�� ó���ǹǷ�, ��Ȯ���� ���̱� ���� �ݿø��̶���� �۾��� �ʿ�.
				}
			}
		}
		endCompareTime = System.currentTimeMillis();
		compareTime += endCompareTime - startCompareTime; 
		///////////////////////////////////////////////////////////����� �� �� ��/////////////////////////////////////////////////////////

		System.out.println("���� �ϴ� ����: " + referenceNumber);
		//System.out.println("���� ���ϴ� ����: " + NonReferenceNumber);
		//System.out.println("�� ��� ���� : "+ PreProcess.lineNumber);

		
		startHashTime = System.currentTimeMillis();
		
		//�̺κ�----------------------------------------------
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
			//������� �����Ǹ� �ȵ� �������� �����ȴٸ�, �ǳʶٱ�. 
			
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
			} // �ѹ� ������ ���� ������ �ߺ� �Ǵ°��� ��� �����ϴ� ���� �����ϱ� ���� true�� �ٲ�
			
			if(sameFlag == true) // �ߺ����� �ʰԲ� �ǳʶ�.
				continue;

			//System.out.println("minAccuracy : " + PreProcess.frameArray.get(i).accuracy);
			
			if(minAccuracy >= PreProcess.frameArray.get(referenceIndex.get(i)).accuracy)
			{
				minAccuracy = PreProcess.frameArray.get(referenceIndex.get(i)).accuracy;
				minAccuracyIndex = referenceIndex.get(i);
				
				// �ε����� ���� ����.

				if(minAccuracy == 0)
				{
					randomIndex.add(referenceIndex.get(i));
				}
			}
		} // for�� ��
		
		

		
		System.out.println("minAccuracy ���� : " + minAccuracy);
		//System.out.println("minAccuracy �ּҴ� :" + minAccuracyIndex);
		
		if(noneReference.size() <= 200)
		{
			noneReference.add(PreProcess.frameArray.get(minAccuracyIndex).currentFrame);
		}
		else
			noneReference.clear(); // ����� 200�� �Ѿ�� ���ݱ��� �����ߴ� �͵� ���� �ʱ�ȭ..

		
		if(minAccuracy == 0)
		{
			int randomValue = (int) (Math.random() * randomIndex.size());

			//System.out.println("������ : "+randomIndex.size());
			//System.out.println("�� : "+randomValue);
			
			for(int i = 0; i < referenceIndex.size(); i++)
			{
				if(referenceIndex.get(i) == randomIndex.get(randomValue))
					continue; // �̰� ������ ������ ����̹Ƿ� �װ� �ʱ�ȭ ���� ����..
				
				PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap = null; // ���� �ȵ� ����� influence map = null
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
					continue; // �̰� ������ ������ ����̹Ƿ� �װ� �ʱ�ȭ ���� ����..
				
				PreProcess.frameArray.get(referenceIndex.get(i)).myLocalInfluenceMap = null; // ���� �ȵ� ����� influence map = null
				PreProcess.frameArray.get(referenceIndex.get(i)).enLocalInfluenceMap = null;
				PreProcess.frameArray.get(referenceIndex.get(i)).isCreateIM = false;
			}
			endHashTime = System.currentTimeMillis();
			
			hashTime += endHashTime - startHashTime;
			return PreProcess.frameArray.get(minAccuracyIndex);
		}
		// ���� ����� ������ �Ѱ��ֱ�
	}

	// 32 -1 134 87 180 0 -100 15 -3 1099 397 180 0 -100 0 // String�� ����.
	/** output.txt�� ���� ������ �о� �� ���ֵ鿡�� ������ �־��ֱ�*/
	public void createUnit()
	{
		String[] splitLine = line.split("-100"); // -100������ �ɰ�. �� ���� ������ �ɰ�

		for(int i = 0; i < splitLine.length; i++)
		{
			String[] splitSpace = splitLine[i].split(" "); // �����̽� �ٷ� ����
			
			if(splitSpace.length <= 2)
			{
				break;
			} // ������ ������ ���̸�, ���� �����Ƿ� ����.
			
			if(Integer.parseInt(splitSpace[1]) != 999999) // 0�̸� ID�� �����Ƿ� ������ ���� ��.
			{			
				int id, type, x, y, health, orderID;
				
				id = Integer.parseInt(splitSpace[1]);
				type = Integer.parseInt(splitSpace[2]);
				x = Integer.parseInt(splitSpace[3]);
				y = Integer.parseInt(splitSpace[4]);
				health = Integer.parseInt(splitSpace[5]);
				orderID = Integer.parseInt(splitSpace[6]);
				// �������� �ɰ��� �� �����鿡 ����.

				if(type >= -4) // �ӽ� �ڵ�
					unitArray.add(new UnitInfo(id, type, x, y, health, orderID, 0, 0, false)); // ���� �߰�.
				
				//System.out.println("id : "+unitArray.get(count).id+" type : "+unitArray.get(count).type+" x : "+unitArray.get(count).x+" y : "+unitArray.get(count).y);	
			}
		}
	}

	/** ����� ������� ��ó���� �Ͽ� �����̵� ���� �� ������ ��ġ üũ */
	public void DataProcessing(Frames nextFrame) // �Ķ���� ���� �ִ� ������, ���� Frames�� ���� ���� �����ϱ� ����.
	{
		/** UnitArray��ŭ ���Ƽ� ���� ��ȯ�� ���� ��ġ, ���� �̵����� �� üũ�ϱ�*/
		for(int i = 0; i < unitArray.size(); i++)
		{
			setPosition(); // ������ �� ����
			
			for(int j = 0; j < nextFrame.unitArray.size(); j++) // ���� for��, �̿��Ͽ� 2���� ���ָ���Ʈ�� ���Ͽ� ID�� ã�Ƴ��� ���
			{
				if(unitArray.get(i).id == nextFrame.unitArray.get(j).id)
				{
					boolean zeroCheck = false;
					
					double rad = Math.atan2(nextFrame.unitArray.get(j).x - unitArray.get(i).x, nextFrame.unitArray.get(j).y - unitArray.get(i).y); // ���� ��� ��
					if(nextFrame.unitArray.get(j).x == unitArray.get(i).x && nextFrame.unitArray.get(j).y == unitArray.get(i).y)
					{
						zeroCheck = true;
					} // �̺κ��� ������ ������ �ϳ��� �ȿ������� �� 5�� �������� ���� ������, �װ��� üũ���༭ 5������ �Ȱ��Բ� �ϱ� ����.
					double degree = (rad * 180.0)/Math.PI; // ���� ��� ��
					unitArray.get(i).nextDirection = ConvertDegree(degree, zeroCheck); // ���� ������ ���� ���� ����
					
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
			
			unitArray.get(i).position = x + y * PreProcess.SIZE_X; // ������ ���ϴ� ��
		}
	}
	
	/** ����� �� ����*/
	public void InfluenceProcessing()
	{
		for(int i = 0; i < unitArray.size(); i++)
		{
			int y = unitArray.get(i).position % PreProcess.SIZE_X;
			int x = unitArray.get(i).position / PreProcess.SIZE_X;
			// position�� 2���� �迭������ �ٲ�
			
			switch(unitArray.get(i).type)
			{
			case -1:
				DiamondInfluence(i, x, y, PreProcess.DRAGOON_INFLUENCE_RANGE, false);
				break; // �Ʊ� ���
			case -2:
				DiamondInfluence(i, x, y, PreProcess.ZEALOT_INFLUENCE_RANGE, false);
				break; // �Ʊ� ����
			case -3:
				DiamondInfluence(i, x, y, PreProcess.DRAGOON_INFLUENCE_RANGE, true);
				break; // ���� ���
			case -4:
				DiamondInfluence(i, x, y, PreProcess.ZEALOT_INFLUENCE_RANGE, true);
				break; // ���� ����
			default:
				break;
			}
		}
	}
	
	/**����� �� �����. currentIndex�� ������ ���� Array ��ȣ. x�� y�� �迭���� ��ǥ. influence�� �����(�Ŀ�), enemy�� ������ �ƴ��� �Ǵ� */
	public void DiamondInfluence(int currentIndex,int x, int y, int influence, boolean enemy)
	{
		for(int i = x - influence; i < x + influence; i++)
		{
			for(int j = y - influence; j < y + influence; j++) // �� -influence���� + influence���� ã��, ĭ�����̰� 4�̻� ���°��� ���� ��ó��
			{
				if(i < 0 || j < 0 || j >= PreProcess.SIZE_X || i >= PreProcess.SIZE_Y) // �ε��� ���� ã��
					continue;

				switch(Math.abs(x - i) + Math.abs(y - j)) // ���� ���� ��ǥ�� �� ��ǥ���� �Ÿ� ����. ��ǥ�� �� ����..
				{
				// ����� ����Ŀ��� ������ �������� ��� ��.
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
	
	/** ����� �� Ȯ��. �� �ػ󵵸� ����*/
	public void ExpandInfluence(int level)
	{
		for(int i = 0; i < unitArray.size(); i++)
		{
			//�켱 ������ ī�ǵ� �������� �ƴ��� �Ǵ�.
			if(unitArray.get(i).copyUnit == false)
			{
				for(int j = unitArray.get(i).x - level * PreProcess.DIVIDE_SIZE; j <= unitArray.get(i).x + level * PreProcess.DIVIDE_SIZE; j += PreProcess.DIVIDE_SIZE)
				{
					for(int k = unitArray.get(i).y - level * PreProcess.DIVIDE_SIZE; k <= unitArray.get(i).y + level * PreProcess.DIVIDE_SIZE; k += PreProcess.DIVIDE_SIZE)
					{
						if(j < 0 || k < 0)
							continue; // x,y��ǥ �������� üũ
						if(unitArray.get(i).type == -1 || unitArray.get(i).type == -2) // �Ʊ� ���� �̾�� ��ü ����
						{
							unitArray.add(new UnitInfo(unitArray.get(i).id, unitArray.get(i).type, j, k, unitArray.get(i).health,
									unitArray.get(i).orderID, 0, unitArray.get(i).nextDirection, true));
							//System.out.println("��ü�� �����Ǿ����ϴ�");
						}
					}
				} //�������� ���� ����
			}
		}
		setPosition(); // ������ ���ֵ��� ������ ����
	}
	
	public double HealthValue(int type, int currentHealth)
	{
		double value = 1;
		
		if(type == -1 || type == -3) // ����̸�
		{
			//value = (double)currentHealth / (double)PreProcess.DRAGOON_HEALTH;
			if((double)currentHealth / (double)PreProcess.DRAGOON_HEALTH >= 0.5)
			{
				value = 1;
			} // ���� ü���� 50% ~ 100%�̸� ���ϴ� �� 1
			else if((double)currentHealth / (double)PreProcess.DRAGOON_HEALTH >= 0.3)
			{
				value = 0.7;
			} // ���� ü���� 30% ~ 50%�̸� ���ϴ� �� 0.7
			else
			{
				value = 0.5;
			} // ���� ü���� 0% ~ 30%�̸� ���ϴ� �� 0.5
			
		}
		else if(type == -2 || type == -4) // �����̸�
		{
			//value = (double)currentHealth / (double)PreProcess.ZEALOT_HEALTH;
			if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.6)
			{
				value = 1;
			} // ���� ü���� 80% ~ 100%�̸� ���ϴ� �� 1
			else if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.3)
			{
				value = 0.7;
			} // ���� ü���� 50% ~ 80% �̸� ���ϴ� �� 0.6
			else if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.2)
			{
				value = 0.5;
			} // ���� ü���� 30% ~ 50% �̸� ���ϴ� �� 0.4
			else if((double)currentHealth / (double)PreProcess.ZEALOT_HEALTH >= 0.1)
			{
				value = 0.35;
			} // ���� ü���� 30% ~ 50% �̸� ���ϴ� �� 0.4
			else
			{
				value = 0.2;
			}// ���� ü���� 0% ~ 20% �̸� ���ϴ� �� 0.2
			
		}
		
		return value;
	}
	
	
	public void WriteFile(int part) throws IOException
	{
		RandomAccessFile out = new RandomAccessFile(PreProcess.address, "rw");
		out.seek(out.length()); // ���� ��ġ ����
		
		if(part == 0) // ������ġ�� �������� ����
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
			} // id, ������ġ, ���û���, �������� ���� 
			
			out.writeBytes("\r\n"); // ���� ����
		}
		
		else if(part == 1) // ����� �� ����
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
	
	// 1 -> ���� 7-> ���� 3-> ���� 5-> ���� 0-> ������ ����?
	/** ��Ÿũ����Ʈ �� �´� ���� üũ�Ͽ� ���� ����.*/
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
