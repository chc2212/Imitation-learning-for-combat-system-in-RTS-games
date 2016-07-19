package pre;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

//5.27 JNIBWAPI에 3프레임 마다 실행하는거 주석 처리함.
//ExampleAIClient가 3프레임 마다 실행되게 추가.


//프레임이 같은 것. 데이터가 똑같은 것이 몇개 있나를 체크해서, 1번 공격인지 2번 공격했는지 체크하는 것도 필요 할지도 모름.
//이동 참조 할떄, expandLevel될 때 어떤 것을 참조하는지, 가장 많은 것을 참조하게 해야함. 해결 필요.
//체력 계산하는 식 고침 필요

// 부분 모방
// 해쉬. 영향력의 합 계산.

// 맵에 미치는 드라군의 영향 280(285?), 질럿의 영향 190

public class PreProcess {
	
	public static String address = "Output.txt";
	public static int fileSize = 3; // 리플레이로 output이 나온 파일 수
	public static int frameTime = 3; // 프레임 타임. 얼마마다 유닛을 체크해주었는지,
	
	public static final int minHashValue = 100; //
	public static final int maxHashValue = 1100;
	
	public static final int minUnitDistancehashValue = 0;
	public static final int maxUnitDistancehashValue = 4; // 500 ~ 1500 | 320 ~ 960 640~1920
	
	
	public static final float minDifferUnitCount = 0;
	public static final float maxDifferUnitCount = 4;
	
	public static float maxReferenceCount = 2000;
	
	public static int unitDistancehashValue = 1;
	public static int hashValue = 400; // 해쉬 영향력 차이 값.
	public static float differUnitCount = 1;
	
	// 64 x 64 지형의 경우 최대 크기는 2047 x 2047 이 된다. 그러므로 한칸의 크기는 32x32이다.
	
	public static final int DIVIDE_SIZE = 64; // 맵을 몇으로 나눌지 정사각형 한칸의 크기를 몇으로 할지.
	public static final int SIZE_X = 4096 / DIVIDE_SIZE; // DIVIDE_SIZE로 나눴을 때 X의 크기
	public static final int SIZE_Y = 4096 / DIVIDE_SIZE; // DIVIDE_SIZE로 나눴을 때 Y의 크기
	//10 -> [204, 84] // 20 -> [ 102, 42] // 40 -> [51, 21]
	public static final int DRAGOON_POWER = 5;
	public static final int DRAGOON_INFLUENCE_RANGE = 7;
	public static final int ZEALOT_POWER = 4;
	public static final int ZEALOT_INFLUENCE_RANGE = 5;
	public static final int ENEMY_DRAGOON_POWER = 5;
	public static final int ENEMY_ZEALOT_POWER = 4;
	public static final int DRAGOON_HEALTH = 180;
	public static final int ZEALOT_HEALTH = 160;
	public static int LIMITLINE = 500000; // 씬 개수 제한 // 추가실험 때 1600 3200 4800 8000 16000 32000
	
	
	public static ArrayList<Frames> frameArray = new ArrayList<Frames>();
	public static ArrayList<Integer> nonReferenceFrameNumber = new ArrayList<Integer>(); // 참조하지 않는 개수

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
		} // hashtable 초기화
		
		
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
				/** 줄수 만큼 만들어줌 */
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

					
					
					frameArray.add(new Frames(lineNumber * frameTime, false)); // Frame 1개 List 생성. Frame수를 곱해줌.
					frameArray.get(lineNumber).line = s; // 한줄을 line에 넣음
					
					frameArray.get(lineNumber).createUnit();
					
					//System.out.println(lineNumber);
		
					lineNumber++;
					
					if(lineNumber >= LIMITLINE) // 씬 개수 제한.
						break;
				} // 텍스트 값들을 객체에 넣어주기.
	
				in.close();
				nonReferenceFrameNumber.add(lineNumber-1);
				nonReferenceFrameNumber.add(lineNumber); // 제일 끝줄 참조 안되게 함.
				
				if(lineNumber >= LIMITLINE)
					break;
				
			}
		}
		System.out.println("줄수"+lineNumber);
		
		for(int i = 0; i < frameArray.size() - 1; i++)
		{
			frameArray.get(i).DataProcessing(frameArray.get(i+1));
		} // 데이터 정제 
		
		for(int i = 0; i < frameArray.size(); i++)
		{
			frameArray.get(i).SumInfluence();
			frameArray.get(i).ComputeNumberOfTotalUnit(); // 아군 적군 유닛 개수 계산.
		} // 영향력 맵 합 계산
		
		for(int i = 0; i < frameArray.size(); i++)
		{
			frameArray.get(i).DistanceAverage();
		}// 현재 유닛들의 거리 평균 계산.

		
		for(int i = 0; i < frameArray.size(); i++)
		{
			if(frameArray.get(i).countMyUnit < 0 || frameArray.get(i).myAverageUnitX / 64 < 0 || frameArray.get(i).myAverageUnitX / 64 >= 64
					|| frameArray.get(i).myAverageUnitY / 64 < 0 || frameArray.get(i).myAverageUnitY / 64 >= 64)
				continue; // 배열 인덱스 오류 검사.
			
			hashtable[frameArray.get(i).countMyUnit][frameArray.get(i).myAverageUnitX / 64][frameArray.get(i).myAverageUnitY / 64].table.add(i);
		}
		
	}

}
