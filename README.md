#Imitation-learning-for-combat-system-in-RTS-games
Unlike the situation with regard to board games, artificial intelligence (AI) for real-time strategy (RTS) games
usually suffers from an infinite number of possible future states. Furthermore, it must handle the complexity quickly. This
constraint makes it difficult to build AI for RTS games with current state-of-the-art intelligent techniques. This research
proposes the use of imitation learning based on a human player’s replays, which allows the AI to mimic the behaviors.
During game play, the AI exploits the replay repository to search for the best similar moment from an influence map
representation. This work focuses on combat in RTS games, considering the spatial configuration and unit types.
Experimental results show that the proposed AI can defeat well-known competition entries a large percentage of the time. 
#Overview of Method
<img src="https://github.com/chc2212/Imitation-learning-for-combat-system-in-RTS-games/blob/master/pic1.png" width="500">
#Features
*	A combat system for RTS games by imitating human players’ micromanagement skills based on spatial analysis. 
*	Case-based reasoning approach to design the system.
*	Influence map representation to analyze the influence of units spatially and to allow for high-speed case comparison.
*	Showed that the proposed approach outperforms competitive entries in StarCraft competitions for combat situations.

#Screen Captures
<img src="https://github.com/chc2212/Imitation-learning-for-combat-system-in-RTS-games/blob/master/20160908_222711.gif" width="350">

#References
* [I.-S. Oh, **H.-C. Cho**, and K.-J. Kim, “Playing real-time strategy games by imitating human players’ micromanagement skills based on spatial analysis,” Expert Systems with Applications, vol. 71, pp. 192-205, 2016.](http://www.sciencedirect.com/science/article/pii/S0957417416306613)
* [I.-S. Oh, **H.-C. Cho** and K.-J. Kim, “Imitation learning for combat system in RTS games with application to StarCraft,” IEEE Conference on Computational Intelligence and Games, 2014.](http://cilab.sejong.ac.kr/home/lib/exe/fetch.php?media=public:paper:cig_2014_cho.pdf)
* K.-J. Kim, **H.-C. Cho** and I.-S. Oh, “System and Method for Generating Artificial Intelligence of Game Character based on Imitation of Game Play Data,” South Korea Patent No. 10-1603681-0000, issued March 2016.
