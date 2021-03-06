package org.gicentre.vast2012.bomnetworkstatus.ui.bugview;

import java.awt.Color;
import java.awt.Font;
import java.io.FileInputStream;
import java.util.Properties;

import org.gicentre.utils.colour.ColourTable;
import org.gicentre.utils.move.ZoomPanState;
import org.gicentre.vast2012.bomnetworkstatus.Businessunit;
import org.gicentre.vast2012.bomnetworkstatus.Facility;
import org.gicentre.vast2012.bomnetworkstatus.ui.BusinessunitGrid;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * Contains common routines and non-public parameters used in all three views
 * 
 * @author Alexander Kachkaev <alexander.kachkaev.1@city.ac.uk>
 */

/* 
 * This file is part of BoM Network Status Application, VAST 2012 Mini Challenge 1 entry
 * awarded for "Efficient Use of Visualization". It is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * BoM Network Status is distributed WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this
 * source code (see COPYING.LESSER included with this source code). If not, see 
 * http://www.gnu.org/licenses/.
 * 
 * For report on challenge, video and summary paper see http://gicentre.org/vast2012/
 */

public abstract class CommonBUGView extends AbstractBUGView {

	protected static PFont captionFont = new PFont(new Font("Helvetica", 0, 18), true);
	protected static PFont legendFont1 = new PFont(new Font("Arial", 0, 12), true);
	protected static PFont legendFont2 = new PFont(new Font("Arial", 0, 9), true);

	protected BusinessunitGrid bug;

	ColourTable activityFlagCT;
	ColourTable policyStatusCT;
	ColourTable connectionCT;

	protected static int FILL_NODATA = 0; // Colour used when no data presents
	protected static float RELAXATION_RATE = 0; // Coefficient that makes colours more relaxed in the snapshot view
	

	public CommonBUGView(BusinessunitGrid grid) {
		bug = grid;

		// Initializing colour tables
		if (activityFlagCT == null) {
			Properties properties = new Properties();
			try {
				try {
					properties.load(new FileInputStream("data/bomnetworkstatus_config.properties"));
				} catch (Exception e) {
					properties.load(new FileInputStream("bomnetworkstatus_config.properties"));
				}
			} catch (Exception e) {
				System.err.println("Error opening config file with colours: " + e.getMessage());
				System.exit(1);
			}

			String propertyName = null;
			
			try {
				propertyName = "colours.nodata";
				FILL_NODATA = Color.decode(properties.getProperty(propertyName)).getRGB();
				propertyName = "colours.relaxation_rate";
				RELAXATION_RATE = Float.parseFloat(properties.getProperty(propertyName));
			} catch (Exception e) {
				System.err.println("Error loading " + propertyName + ": " + properties.getProperty(propertyName)
						+ ". Make sure the property is defined in config.properties and has a valid value.");
				System.exit(1);
			}
			
			activityFlagCT = new ColourTable();
			for (int i = 0; i <= 5; i++) {
				propertyName = "colours.activity_flag." + i;
				try {
					activityFlagCT.addContinuousColourRule(i, Color.decode(properties.getProperty(propertyName)).getRGB());
				} catch (Exception e) {
					System.err.println("Error loading colour " + propertyName + ": " + properties.getProperty(propertyName)
							+ ". Make sure the property is defined in config.properties and has a valid value.");
					System.exit(1);
				}
			}
			policyStatusCT = new ColourTable();
			for (int i = 0; i <= 5; i++) {
				propertyName = "colours.policy_status." + i;
				try {
					policyStatusCT.addContinuousColourRule(i, Color.decode(properties.getProperty(propertyName)).getRGB());
				} catch (Exception e) {
					System.err.println("Error loading colour " + propertyName + ": " + properties.getProperty(propertyName)
							+ ". Make sure the property is defined in config.properties and has a valid value.");
					System.exit(1);
				}
			}

			connectionCT = new ColourTable();
			connectionCT.addContinuousColourRule(0, Color.HSBtoRGB(0, 0, 0));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void draw(PGraphics canvas, ZoomPanState zps, Thread thread) {
		drawLayout(canvas);
		for (int i = 0; i < bug.getColCount(); i++)
			for (int j = 0; j < bug.getRowCount(); j++)
				if (bug.getBusinessunitNameAt(i, j) != null)
					drawBusinessunit(canvas, bug.getBusinessunitNameAt(i, j), zps, thread);
		drawLabels(canvas);
	}

	/**
	 * Draws the layout of the grid
	 */
	protected void drawLayout(PGraphics canvas) {
		canvas.background(240);
		// canvas.rect(bug.x, bug.y, bug.getWidth(), bug.getHeight());
		// canvas.noStroke();
		// for (int i = 0; i < bug.getColCount(); i++)
		// for (int j = 0; j < bug.getRowCount(); j++)
		// canvas.rect(bug.getBuX(i), bug.getBuY(j), bug.getColWidth(), bug.getRowHeight());
	}

	/**
	 * Draws the labels of the business units
	 */
	protected void drawLabels(PGraphics canvas) {
		if (!showLabels)
			return;

		canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
		canvas.textFont(captionFont);

		for (int i = 0; i < bug.getColCount(); i++)
			for (int j = 0; j < bug.getRowCount(); j++) {
				Businessunit bu = bug.getBusinessunitAt(i, j);
				if (bu == null)
					continue;
				canvas.fill(0, 128);
				canvas.text(bu.name, bug.getColX(i) + BusinessunitGrid.COL_WIDTH / 2, bug.getColY(j) + BusinessunitGrid.ROW_HEIGHT / 2);
				canvas.fill(255, 50);
				canvas.text(bu.name, bug.getColX(i) + BusinessunitGrid.COL_WIDTH / 2, bug.getColY(j) + BusinessunitGrid.ROW_HEIGHT / 2);
			}
	}

	protected void drawArrow(PGraphics canvas, int x, int y, int orientation) {
		drawArrow(canvas, x, y, orientation, 0);
	}

	protected void drawArrow(PGraphics canvas, int x, int y, int orientation, int wing) {
		canvas.pushMatrix();
		canvas.translate(x, y);

		switch (orientation) {
		case PApplet.LEFT:
			canvas.translate(1, 1);
			canvas.rotate((float) (Math.PI));
			break;
		case PApplet.RIGHT:
			// canvas.translate(0, 0);
			break;
		}

		canvas.fill(0);
		canvas.noStroke();
		for (int i = 1; i < 5; i++) {
			switch (wing) {
			case PApplet.LEFT:
				canvas.rect(i, 0, 1, i);
				break;
			case PApplet.RIGHT:
				canvas.rect(i, 1, 1, -i);
				break;
			default:
				canvas.rect(i, 1 - i, 1, 1 + (i - 1) * 2);
				break;
			}
		}

		canvas.popMatrix();
	}

	protected void drawSelectionHighlighter(PGraphics p, int x, int y, int w, int h) {
		if (h == 1) {
			drawArrow(p, x, y, PApplet.LEFT);
			drawArrow(p, x + w - 1, y, PApplet.RIGHT);
		} else {
			drawArrow(p, x + w - 1, y - 1, PApplet.RIGHT, PApplet.RIGHT);
			drawArrow(p, x + w - 1, y + h, PApplet.RIGHT, PApplet.LEFT);
			drawArrow(p, x, y - 1, PApplet.LEFT, PApplet.LEFT);
			drawArrow(p, x, y + h, PApplet.LEFT, PApplet.RIGHT);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectAt(int x, int y) {
		// Resetting selected facility
		selectedFacility = null;

		// Looking for a businessunit
		Businessunit selectedBU = null;
		for (int i = 0; i < bug.getColCount(); i++)
			for (int j = 0; j < bug.getRowCount(); j++)
				if (bug.getBusinessunitAt(i, j) != null && x >= bug.getColX(i) && x < bug.getColX(i) + BusinessunitGrid.COL_WIDTH && y >= bug.getColY(j)
						&& y < bug.getColY(j) + bug.getBusinessunitHeight(bug.getBusinessunitAt(i, j))) {
					selectedBU = bug.getBusinessunitAt(i, j);
					break;
				}
		if (selectedBU == null)
			return;

		// Looking for a facility
		int localY = y - bug.getBusinessunitY(selectedBU.name);
		int currentY = 0;
		Facility prevFacility = null;
		for (Facility f : selectedBU.sortedFacilities) {
			currentY += bug.getGapBetweenFacilities(prevFacility, f);

			// y is in the gap
			if (currentY > localY)
				return;
			currentY += bug.getFacilityHeight(f);

			// y is above the current facility
			if (currentY > localY) {
				selectedFacility = f;
				return;
			}
			prevFacility = f;
		}
	}

	public int getColour(int parameter, int value) {
		return getColour(parameter, value, true);
	}

	public int getColour(int parameter, int value, boolean relaxed) {
		ColourTable ct;
		switch (parameter) {
		case P_ACTIVITYFLAG:
			ct = activityFlagCT;
			break;
		case P_POLICYSTATUS:
			ct = policyStatusCT;
			break;
		default:
			ct = connectionCT;
			break;
		}

		if (relaxed)
			return PApplet.lerpColor(ct.findColour(value), 0xffffffff, RELAXATION_RATE, PApplet.BLEND);
		else
			return ct.findColour(value);
	}

	public int getConnectionsColour(PGraphics canvas, int numConnections) {
		double cRangeMin = 0;
		double cRangeMax = 120;

		if (currentParameter == P_CONNECTIONS) {
			cRangeMin = rangeMin;
			cRangeMax = rangeMax;
		}

		return canvas.lerpColor(0xffffffff, getColour(AbstractBUGView.P_CONNECTIONS, 0, false),
				(float) Math.min(1, Math.max(0, (numConnections) - cRangeMin) / (cRangeMax - cRangeMin)));
	}

	public boolean selectNeighbourFacility(int diff) {
		Businessunit bu = bug.getBusinessunits().get(selectedFacility.businessunitName);
		int oldPos = bu.sortedFacilities.indexOf(selectedFacility);
		int pos = oldPos + diff;

		if (pos < 0)
			pos = 0;

		if (pos >= bu.sortedFacilities.size())
			pos = bu.sortedFacilities.size() - 1;

		if (pos == oldPos)
			return false;

		selectedFacility = bu.sortedFacilities.get(pos);

		if (timeIsRelative) {
			selectedCompactTimestamp += (bu.sortedFacilities.get(pos).timezoneOffset - selectedFacility.timezoneOffset) * 4;
		}

		return true;
	}

	public boolean selectNeighbourTimestamp(int diff) {
		if (this instanceof SnapshotBUGView)
			currentCompactTimestamp += diff;
		selectedCompactTimestamp += diff;
		return true;
	}

	public void drawGradientLegend(PGraphics canvas, float x, float y, float width, float height) {

		canvas.pushMatrix();
		canvas.translate(x, y);

		String t1, t2;

		if (rangeIsAbsolute) {
			t1 = String.valueOf((int) rangeMin);
			t2 = String.valueOf((int) rangeMax);
		} else {
			t1 = String.valueOf((int) Math.round(rangeMin * 100)) + "%";
			t2 = String.valueOf((int) Math.round(rangeMax * 100)) + "%";
		}

		canvas.fill(120);
		canvas.textAlign(PGraphics.LEFT, PGraphics.TOP);
		canvas.text(t1, 0, 0);
		canvas.textAlign(PGraphics.RIGHT, PGraphics.TOP);
		canvas.text(t2, width, 0);

		canvas.noStroke();
		int colourMax = getColour(currentParameter, currentValue, false);
		int colourMin = canvas.color(255, 255, 255);

		for (float i = 0; i < width; i++) {
			canvas.fill(canvas.lerpColor(colourMin, colourMax, i / width));
			canvas.rect(i, 16, 1, height - 16);
		}

		canvas.popMatrix();
	}

	public void drawSequentialLegend(PGraphics canvas, float x, float y, float width, float height) {

		canvas.pushMatrix();
		canvas.translate(x, y);

		canvas.textAlign(PGraphics.CENTER, PGraphics.TOP);
		x += canvas.textWidth("0");
		width -= canvas.textWidth("5");

		for (int i = 0; i <= 5; i++) {
			String caption = String.valueOf(i);
			float currentY = x + (width - x) * i / 5;
			canvas.fill(120);
			canvas.text(caption, currentY, 0);
			canvas.fill(getColour(currentParameter, i));
			canvas.rect(currentY - canvas.textWidth(caption) / 2, 16, canvas.textWidth(caption), 3);
		}
		canvas.popMatrix();
	}

	public void drawStatsLegend(PGraphics canvas, float x, float y, float width, float height) {

		canvas.pushMatrix();
		canvas.translate(x, y);

		String t1, t2;

		if (rangeIsAbsolute) {
			t1 = String.valueOf((int) rangeMin);
			t2 = String.valueOf((int) rangeMax);
		} else {
			t1 = String.valueOf((int) (rangeMin * 100)) + "%";
			t2 = String.valueOf((int) (rangeMax * 100)) + "%";
		}

		canvas.fill(120);
		canvas.textAlign(PGraphics.LEFT, PGraphics.TOP);
		canvas.text(t1, 0, 0);
		canvas.textAlign(PGraphics.RIGHT, PGraphics.TOP);
		canvas.text(t2, width, 0);

		canvas.noStroke();

		// Connections
		int minX = 30;
		int maxX = 160;
		int avgX = 80;
		int sdX = 32;

		canvas.textFont(legendFont2);
		canvas.textAlign(PGraphics.LEFT);
		canvas.text("min", minX, 12);
		canvas.textAlign(PGraphics.CENTER);
		canvas.text("avg", avgX, 12);
		canvas.textAlign(PGraphics.RIGHT);
		canvas.text("sd", avgX + sdX, 12);
		canvas.text("max", maxX, 12);

		// Min - Max
		float minXCorrected = Math.max(Math.min(minX, 191), 0);
		float maxXCorrected = Math.max(Math.min(maxX, 191), 0);
		canvas.fill(230);
		canvas.rect(minXCorrected, 16, maxXCorrected - minXCorrected, 3);

		// Sd
		canvas.fill(200);
		int sdLeft = Math.max(Math.min(avgX - sdX, 191), 0);
		int sdRight = Math.max(Math.min(avgX + sdX, 191), 0);
		canvas.rect(sdLeft, 16, sdRight - sdLeft, 3);

		// Avg
		canvas.fill(0);
		canvas.rect(Math.max(Math.min(avgX, 191), 0), 16, 1, 3);

		canvas.popMatrix();
	}

	public void resetRange() {
		switch (currentParameter) {
		case P_ACTIVITYFLAG:
		case P_POLICYSTATUS:
			rangeMin = 0;
			rangeMax = 1;
			rangeMinLimit = 0;
			rangeMaxLimit = 1;
			rangeIsAbsolute = false;
			break;

		default:
			rangeMin = 0;
			rangeMax = 120;
			rangeMinLimit = 0;
			rangeMaxLimit = 120;
			rangeIsAbsolute = true;
			break;
		}
	}
}
