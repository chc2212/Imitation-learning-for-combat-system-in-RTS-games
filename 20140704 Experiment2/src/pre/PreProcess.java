package pre;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

//5.27 JNIBWAPI�� 3������ ���� �����ϴ°� �ּ� ó����.
//ExampleAIClient�� 3������ ���� ����ǰ� �߰�.


//�������� ���� ��. �����Ͱ� �Ȱ��� ���� � �ֳ��� üũ�ؼ�, 1�� �������� 2�� �����ߴ��� üũ�ϴ� �͵� �ʿ� ������ ��.
//�̵� ���� �ҋ�, expandLevel�� �� � ���� �����ϴ���, ���� ���� ���� �����ϰ� �ؾ���. �ذ� �ʿ�.
//ü�� ����ϴ� �� ��ħ �ʿ�

// �κ� ���
// �ؽ�. ������� �� ���.

// �ʿ� ��ġ�� ����� ���� 280(285?), ������ ���� 190

public class PreProcess {
	
	public static String address = "Output.txt";
	public static int fileSize = 3; // ���÷��̷� output�� ���� ���� ��
	public static int frameTime = 3; // ������ Ÿ��. �󸶸��� ������ üũ���־�����,
	
	public static final int minHashValue = 100; //
	public static final int maxHashValue = 1100;
	
	public static final int minUnitDistancehashValue = 0;
	public static final int maxUnitDistancehashValue = 4; // 500 ~ 1500 | 320 ~ 960 640~1920
	
	
	public static final float minDifferUnitCount = 0;
	public static final float maxDifferUnitCount = 4;
	
	public static float maxReferenceCount = 2000;
	
	public static int unitDistancehashValue = 1;
	public static int hashValue = 400; // �ؽ� ����� ���� ��.
	public static float differUnitCount = 1;
	
	// 64 x 64 ������ ��� �ִ� ũ��� 2047 x 2047 �� �ȴ�. �׷��Ƿ� ��ĭ�� ũ��� 32x32�̴�.
	
	public static final int DIVIDE_SIZE = 64; // ���� ������ ������ ���簢�� ��ĭ�� ũ�⸦ ������ ����.
	public static final int SIZE_X = 4096 / DIVIDE_SIZE; // DIVIDE_SIZE�� ������ �� X�� ũ��
	public static final int SIZE_Y = 4096 / DIVIDE_SIZE; // DIVIDE_SIZE�� ������ �� Y�� ũ��
	//10 -> [204, 84] // 20 -> [ 102, 42] // 40 -> [51, 21]
	public static final int DRAGOON_POWER = 5;
	public static final int DRAGOON_INFLUENCE_RANGE = 7;
	public static final int ZEALOT_POWER = 4;
	public static final int ZEALOT_INFLUENCE_RANGE = 5;
	public static final int ENEMY_DRAGOON_POWER = 5;
	public static final int ENEMY_ZEALOT_POWER = 4;
	public static final int DRAGOON_HEALTH = 180;
	public static final int ZEALOT_HEALTH = 160;
	public static int LIMITLINE = 500000; // �� ���� ���� // �߰����� �� 1600 3200 4800 8000 16000 32000
	
	
	public static ArrayList<Frames> frameArray = new ArrayList<Frames>();
	public static ArrayList<Integer> nonReferenceFrameNumber = new ArrayList<Integer>(); // �������� �ʴ� ����

	public static int randomNumber = 500000;
	public static int lineNumber = 0;
	public static float totalMyLocalInfluenceMap[][] = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
	public static float totalEnLocalInfluenceMap[][] = new float[PreProcess.SIZE_Y][PreProcess.SIZE_X];
	
	public static HashTable[][][] hashtable = new HashTable[100][64][64];

	public static void ProcessBuild() throws IOException {

		for(int i = 0; i < 100; i++){
			for(int j = 0; j < 64; j++){
				for(int k = 0; k < 64; k++)
				{
					hashtable[i][j][k] = new HashTable();
				}
			}
		} // hashtable �ʱ�ȭ
		
		
		//int lineNumber = 0;
		
		//RandomAccessFile out = new RandomAccessFile(address, "rw");
		
		//for(int l = 1; l <= 4;  l++)
		{
			for(int k = 1; k <=216; k++)
			{
				BufferedReader in = null;
				
				in = new BufferedReader(new FileReader(new File("DataFile/lowtxt/up170/"+k+".txt")));

				/*if(l == 1)
					in = new BufferedReader(new FileReader(new File("DataFile/12_W"+k+".txt")));
				else if(l == 2)
					in = new BufferedReader(new FileReader(new File("DataFile/24_W"+k+".txt")));
				else if(l == 3)
					in = new BufferedReader(new FileReader(new File("DataFile/36_W"+k+".txt")));
				else if(l == 4)
					in = new BufferedReader(new FileReader(new File("DataFile/MIX_W"+k+".txt")));
				*/	
				
				int a = 0;
				/** �ټ� ��ŭ ������� */
				while(true)
				{
					String s = in.readLine();
					a++;
					
					if(s == null)
						break;
					
					if(a <= 30)
						continue;
					
					
					int randomValue = (int) (Math.random() * randomNumber);
					
					if(randomValue >= LIMITLINE + (LIMITLINE*0.1))
						continue;

					
					
					frameArray.add(new Frames(lineNumber * frameTime, false)); // Frame 1�� List ����. Frame���� ������.
					frameArray.get(lineNumber).line = s; // ������ line�� ����
					
					frameArray.get(lineNumber).createUnit();
					
					//System.out.println(lineNumber);
		
					lineNumber++;
					
					if(lineNumber >= LIMITLINE) // �� ���� ����.
						break;
				} // �ؽ�Ʈ ������ ��ü�� �־��ֱ�.
	
				in.close();
				nonReferenceFrameNumber.add(lineNumber-1);
				nonReferenceFrameNumber.add(lineNumber); // ���� ���� ���� �ȵǰ� ��.
				
				if(lineNumber >= LIMITLINE)
					break;
				
			}
		}
		System.out.println("�ټ�"+lineNumber);
		
		for(int i = 0; i < frameArray.size() - 1; i++)
		{
			frameArray.get(i).DataProcessing(frameArray.get(i+1));
		} // ������ ���� 
		
		for(int i = 0; i < frameArray.size(); i++)
		{
			frameArray.get(i).SumInfluence();
			frameArray.get(i).ComputeNumberOfTotalUnit(); // �Ʊ� ���� ���� ���� ���.
		} // ����� �� �� ���
		
		for(int i = 0; i < frameArray.size(); i++)
		{
			frameArray.get(i).DistanceAverage();
		}// ���� ���ֵ��� �Ÿ� ��� ���.

		
		for(int i = 0; i < frameArray.size(); i++)
		{
			if(frameArray.get(i).countMyUnit < 0 || frameArray.get(i).myAverageUnitX / 64 < 0 || frameArray.get(i).myAverageUnitX / 64 >= 64
					|| frameArray.get(i).myAverageUnitY / 64 < 0 || frameArray.get(i).myAverageUnitY / 64 >= 64)
				continue; // �迭 �ε��� ���� �˻�.
			
			hashtable[frameArray.get(i).countMyUnit][frameArray.get(i).myAverageUnitX / 64][frameArray.get(i).myAverageUnitY / 64].table.add(i);
		}
		
	}

}
