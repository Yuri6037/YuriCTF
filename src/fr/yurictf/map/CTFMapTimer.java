package fr.yurictf.map;

import fr.yurictf.YuriCTF;

public class CTFMapTimer {

    private int maxTicks = 24000;

    public int elapsedTicks = 0;

    public int minutes;
    public int seconds;

    private boolean isRunning;

    public boolean isFinished;

    public YuriCTF thePlugin;

    public CTFMapTimer(YuriCTF map, boolean par1){
        thePlugin = map;
        if (par1){
            isRunning = false;
        } else {
            isRunning = true;
        }
        updateHour();
        map.getLogger().info("Running=" + isRunning);
    }

    private void updateHour(){
        int sec = elapsedTicks / 50;
        int min = sec / 60;
        sec -= (min * 60);
        minutes = min;
        seconds = sec;
    }

    public void updateTimer(){
        if (isRunning){
            elapsedTicks++;
            if (elapsedTicks >= maxTicks && !isFinished){
                thePlugin.terminateGame();
            } else if (elapsedTicks >= 2048 && isFinished){
                thePlugin.onGameEndByTimer();
                isRunning = false;
            }
        }
        updateHour();
    }

    public String getTimeAsString(){
        return minutes + " : " + seconds;
    }

    public boolean isGameRunning(){
        return isRunning;
    }
}
