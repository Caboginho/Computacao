package view;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import utils.Constants;

/**
 * Gerencia a reprodução de efeitos sonoros do jogo
 */
public class SoundManager {
    private Clip moveSound;
    private Clip captureSound;
    private Clip kingSound;
    private Clip winSound;
    private boolean soundsEnabled = true;
    
    public SoundManager() {
        loadSounds();
    }
    
    /**
     * Carrega os arquivos de som
     */
    private void loadSounds() {
        try {
            moveSound = loadSound(Constants.SOUND_MOVE);
            captureSound = loadSound(Constants.SOUND_CAPTURE);
            kingSound = loadSound(Constants.SOUND_KING);
            winSound = loadSound(Constants.SOUND_WIN);
            
            if (moveSound == null && captureSound == null && 
                kingSound == null && winSound == null) {
                soundsEnabled = false;
                System.out.println("Sons não encontrados. Jogo continuará sem áudio.");
            }
        } catch (Exception e) {
            soundsEnabled = false;
            System.out.println("Erro ao carregar sons: " + e.getMessage());
        }
    }
    
    private Clip loadSound(String filename) {
        try {
            File soundFile = new File(filename);
            if (!soundFile.exists()) {
                return null;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            return null;
        }
    }
    
    public void playMoveSound() {
        playSound(moveSound);
    }
    
    public void playCaptureSound() {
        playSound(captureSound);
    }
    
    public void playKingSound() {
        playSound(kingSound);
    }
    
    public void playWinSound() {
        playSound(winSound);
    }
    
    private void playSound(Clip clip) {
        if (soundsEnabled && clip != null) {
            try {
                clip.setFramePosition(0);
                clip.start();
            } catch (Exception e) {
                // Ignora erros de áudio
            }
        }
    }
    
    public boolean isSoundsEnabled() {
        return soundsEnabled;
    }
}