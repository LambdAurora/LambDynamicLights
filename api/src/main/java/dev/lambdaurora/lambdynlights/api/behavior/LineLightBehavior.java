/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Range;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

/**
 * Represents a generic dynamic lighting behavior that takes the shape of a line.
 *
 * @author LambdAurora, Akarys
 * @version 4.0.0
 * @since 4.0.0
 */
public class LineLightBehavior implements DynamicLightBehavior {
	private Vector3d startPoint;
	private Vector3d endPoint;
	private @Nullable Vector3d prevStartPoint;
	private @Nullable Vector3d prevEndPoint;
	private int luminance;
	private int prevLuminance;

	public LineLightBehavior(Vector3d startPoint, Vector3d endPoint, int luminance) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.prevStartPoint = null;
		this.prevEndPoint = null;
		this.luminance = luminance;
	}

	/**
	 * {@return the start point of this line of light}
	 */
	public Vector3d getStartPoint() {
		return this.startPoint;
	}

	/**
	 * Sets the start point of this line of light.
	 *
	 * @param startPoint the start point
	 * @see #setStartPoint(double, double, double)
	 */
	public void setStartPoint(Vector3d startPoint) {
		this.startPoint = startPoint;
	}

	/**
	 * Sets the start point of this line of light.
	 *
	 * @param x the X-coordinate of the start point
	 * @param y the Y-coordinate of the start point
	 * @param z the Z-coordinate of the start point
	 * @see #setStartPoint(Vector3d)
	 */
	public void setStartPoint(double x, double y, double z) {
		this.startPoint.set(x, y, z);
	}

	/**
	 * {@return the end point of this line of light}
	 */
	public Vector3d getEndPoint() {
		return this.endPoint;
	}

	/**
	 * Sets the end point of this line of light.
	 *
	 * @param endPoint the end point
	 * @see #setEndPoint(double, double, double)
	 */
	public void setEndPoint(Vector3d endPoint) {
		this.endPoint = endPoint;
	}

	/**
	 * Sets the end point of this line of light.
	 *
	 * @param x the X-coordinate of the end point
	 * @param y the Y-coordinate of the end point
	 * @param z the Z-coordinate of the end point
	 * @see #setEndPoint(Vector3d)
	 */
	public void setEndPoint(double x, double y, double z) {
		this.endPoint.set(x, y, z);
	}

	/**
	 * {@return the luminance of this line of light}
	 */
	public int getLuminance() {
		return this.luminance;
	}

	/**
	 * Sets the luminance of this line of light.
	 *
	 * @param luminance the luminance value
	 */
	public void setLuminance(int luminance) {
		this.luminance = luminance;
	}

	@Override
	public @Range(from = 0, to = 15) double lightAtPos(BlockPos pos, double falloffRatio) {
		Vector3d line = new Vector3d(this.endPoint).sub(this.startPoint);  // ab
		Vector3d pointToStart = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).sub(this.startPoint);  // av

		if (new Vector3d(pointToStart).dot(line) <= 0.0) {
			return this.luminance - pointToStart.length() * falloffRatio;
		}

		Vector3d pointToEnd = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).sub(this.endPoint);  // bv

		if (new Vector3d(pointToEnd).dot(line) >= 0.0) {
			return this.luminance - pointToEnd.length() * falloffRatio;
		}

		// d = |(end - start) x (start - point)| / |end - start| where x is cross product
		double distance = new Vector3d(line).cross(pointToStart).length() / line.length();
		return this.luminance - distance * falloffRatio;
	}

	@Override
	public boolean hasChanged() {
		if (!this.startPoint.equals(this.prevStartPoint) || !this.endPoint.equals(this.prevEndPoint) || this.luminance != this.prevLuminance) {
			this.prevStartPoint = new Vector3d(this.startPoint);
			this.prevEndPoint = new Vector3d(this.endPoint);
			this.prevLuminance = this.luminance;

			return true;
		}
		return false;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(
				Mth.floor(this.startPoint.x()),
				Mth.floor(this.startPoint.y()),
				Mth.floor(this.startPoint.z()),
				Mth.ceil(this.endPoint.x()),
				Mth.ceil(this.endPoint.y()),
				Mth.ceil(this.endPoint.z())
		);
	}
}
