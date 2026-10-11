package com.payamsdev.oldsona;

import com.badlogic.gdx.*;

import screens.EventCutsceneScreen;
import screens.MenuAnimationTestScreen;
import screens.SliceScreen;

public class OldsonaGame extends Game {
	@Override
	public void create() {
		setScreen(new EventCutsceneScreen(this, "onBoot"));
	}

	public void showLegacy() {
		Screen old = getScreen();
		setScreen(new battle.BattleScreen());
		old.dispose();
	}

	@Override
	public void render() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
			Screen old = getScreen();
			setScreen(new SliceScreen(this));
			old.dispose();
		}
		super.render();
	}

	@Override
	public void dispose() {
		if (getScreen() != null)
			getScreen().dispose();
	}
}
