package org.x2vc.stylesheet.coverage;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;

/**
 * Standard implementation of {@link ICoverageStatistics}.
 */
public final class CoverageStatistics implements ICoverageStatistics {

	@XmlElement(name = "byDirective")
	private final DirectiveStatistics directiveStatistics;

	@XmlElement(name = "byLine")
	private final LineStatistics lineStatistics;

	private CoverageStatistics(Builder builder) {
		this.directiveStatistics = new DirectiveStatistics(
				builder.totalDirectiveCount,
				builder.fullCoverageDirectiveCount,
				builder.partialCoverageDirectiveCount,
				builder.noCoverageDirectiveCount);
		this.lineStatistics = new LineStatistics(
				builder.totalLineCount,
				builder.emptyLineCount,
				builder.fullCoverageLineCount,
				builder.partialCoverageLineCount,
				builder.noCoverageLineCount);
	}

	@Override
	public long getTotalDirectiveCount() {
		return this.directiveStatistics.totalCount;
	}

	@Override
	public long getDirectiveCountWithFullCoverage() {
		return this.directiveStatistics.fullCoverageCount;
	}

	@Override
	public long getDirectiveCountWithPartialCoverage() {
		return this.directiveStatistics.partialCoverageCount;
	}

	@Override
	public long getDirectiveCountWithNoCoverage() {
		return this.directiveStatistics.noCoverageCount;
	}

	@Override
	public double getDirectivePercentageWithFullCoverage() {
		return this.directiveStatistics.fullCoveragePercentage;
	}

	@Override
	public double getDirectivePercentageWithPartialCoverage() {
		return this.directiveStatistics.partialCoveragePercentage;
	}

	@Override
	public double getDirectivePercentageWithNoCoverage() {
		return this.directiveStatistics.noCoveragePercentage;
	}

	@Override
	public long getTotalLineCount() {
		return this.lineStatistics.totalCount;
	}

	@Override
	public long getLineCountEmpty() {
		return this.lineStatistics.emptyCount;
	}

	@Override
	public long getLineCountWithFullCoverage() {
		return this.lineStatistics.fullCoverageCount;
	}

	@Override
	public long getLineCountWithPartialCoverage() {
		return this.lineStatistics.partialCoverageCount;
	}

	@Override
	public long getLineCountWithNoCoverage() {
		return this.lineStatistics.noCoverageCount;
	}

	@Override
	public double getLinePercentageEmpty() {
		return this.lineStatistics.emptyPercentage;
	}

	@Override
	public double getLinePercentageWithFullCoverage() {
		return this.lineStatistics.fullCoveragePercentage;
	}

	@Override
	public double getLinePercentageWithPartialCoverage() {
		return this.lineStatistics.partialCoveragePercentage;
	}

	@Override
	public double getLinePercentageWithNoCoverage() {
		return this.lineStatistics.noCoveragePercentage;
	}

	/**
	 * Creates builder to build {@link CoverageStatistics}.
	 *
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.directiveStatistics, this.lineStatistics);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CoverageStatistics)) {
			return false;
		}
		final CoverageStatistics other = (CoverageStatistics) obj;
		return Objects.equals(this.directiveStatistics, other.directiveStatistics)
				&& Objects.equals(this.lineStatistics, other.lineStatistics);
	}

	/**
	 * Builder to build {@link CoverageStatistics}.
	 */
	public static final class Builder {
		private long totalDirectiveCount;
		private long fullCoverageDirectiveCount;
		private long partialCoverageDirectiveCount;
		private long noCoverageDirectiveCount;
		private long totalLineCount;
		private long emptyLineCount;
		private long fullCoverageLineCount;
		private long partialCoverageLineCount;
		private long noCoverageLineCount;

		private Builder() {
		}

		/**
		 * Builder method for totalDirectiveCount parameter.
		 *
		 * @param totalDirectiveCount field to set
		 * @return builder
		 */
		public Builder withTotalDirectiveCount(long totalDirectiveCount) {
			this.totalDirectiveCount = totalDirectiveCount;
			return this;
		}

		/**
		 * Builder method for fullCoverageDirectiveCount parameter.
		 *
		 * @param fullCoverageDirectiveCount field to set
		 * @return builder
		 */
		public Builder withFullCoverageDirectiveCount(long fullCoverageDirectiveCount) {
			this.fullCoverageDirectiveCount = fullCoverageDirectiveCount;
			return this;
		}

		/**
		 * Builder method for partialCoverageDirectiveCount parameter.
		 *
		 * @param partialCoverageDirectiveCount field to set
		 * @return builder
		 */
		public Builder withPartialCoverageDirectiveCount(long partialCoverageDirectiveCount) {
			this.partialCoverageDirectiveCount = partialCoverageDirectiveCount;
			return this;
		}

		/**
		 * Builder method for noCoverageDirectiveCount parameter.
		 *
		 * @param noCoverageDirectiveCount field to set
		 * @return builder
		 */
		public Builder withNoCoverageDirectiveCount(long noCoverageDirectiveCount) {
			this.noCoverageDirectiveCount = noCoverageDirectiveCount;
			return this;
		}

		/**
		 * Builder method for totalLineCount parameter.
		 *
		 * @param totalLineCount field to set
		 * @return builder
		 */
		public Builder withTotalLineCount(long totalLineCount) {
			this.totalLineCount = totalLineCount;
			return this;
		}

		/**
		 * Builder method for emptyLineCount parameter.
		 *
		 * @param emptyLineCount field to set
		 * @return builder
		 */
		public Builder withEmptyLineCount(long emptyLineCount) {
			this.emptyLineCount = emptyLineCount;
			return this;
		}

		/**
		 * Builder method for fullCoverageLineCount parameter.
		 *
		 * @param fullCoverageLineCount field to set
		 * @return builder
		 */
		public Builder withFullCoverageLineCount(long fullCoverageLineCount) {
			this.fullCoverageLineCount = fullCoverageLineCount;
			return this;
		}

		/**
		 * Builder method for partialCoverageLineCount parameter.
		 *
		 * @param partialCoverageLineCount field to set
		 * @return builder
		 */
		public Builder withPartialCoverageLineCount(long partialCoverageLineCount) {
			this.partialCoverageLineCount = partialCoverageLineCount;
			return this;
		}

		/**
		 * Builder method for noCoverageLineCount parameter.
		 *
		 * @param noCoverageLineCount field to set
		 * @return builder
		 */
		public Builder withNoCoverageLineCount(long noCoverageLineCount) {
			this.noCoverageLineCount = noCoverageLineCount;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public CoverageStatistics build() {
			return new CoverageStatistics(this);
		}
	}

	private static final class DirectiveStatistics {

		@XmlElement
		protected final long totalCount;
		@XmlElement
		protected final long fullCoverageCount;
		@XmlElement
		protected final long partialCoverageCount;
		@XmlElement
		protected final long noCoverageCount;
		@XmlElement
		protected final double fullCoveragePercentage;
		@XmlElement
		protected final double partialCoveragePercentage;
		@XmlElement
		protected final double noCoveragePercentage;

		protected DirectiveStatistics(long totalCount, long fullCoverageCount, long partialCoverageCount,
				long noCoverageCount) {
			super();
			this.totalCount = totalCount;
			this.fullCoverageCount = fullCoverageCount;
			this.partialCoverageCount = partialCoverageCount;
			this.noCoverageCount = noCoverageCount;

			if (totalCount == 0) {
				this.fullCoveragePercentage = 0;
				this.partialCoveragePercentage = 0;
				this.noCoveragePercentage = 0;
			} else {
				this.fullCoveragePercentage = (fullCoverageCount * 100f) / totalCount;
				this.partialCoveragePercentage = (partialCoverageCount * 100f) / totalCount;
				this.noCoveragePercentage = (noCoverageCount * 100f) / totalCount;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.fullCoverageCount, this.fullCoveragePercentage, this.noCoverageCount,
					this.noCoveragePercentage,
					this.partialCoverageCount, this.partialCoveragePercentage, this.totalCount);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DirectiveStatistics)) {
				return false;
			}
			final DirectiveStatistics other = (DirectiveStatistics) obj;
			return this.fullCoverageCount == other.fullCoverageCount
					&& Double.doubleToLongBits(this.fullCoveragePercentage) == Double
						.doubleToLongBits(other.fullCoveragePercentage)
					&& this.noCoverageCount == other.noCoverageCount
					&& Double.doubleToLongBits(this.noCoveragePercentage) == Double
						.doubleToLongBits(other.noCoveragePercentage)
					&& this.partialCoverageCount == other.partialCoverageCount
					&& Double.doubleToLongBits(this.partialCoveragePercentage) == Double
						.doubleToLongBits(other.partialCoveragePercentage)
					&& this.totalCount == other.totalCount;
		}
	}

	private static final class LineStatistics {

		@XmlElement
		protected final long totalCount;
		@XmlElement
		protected final long emptyCount;
		@XmlElement
		protected final long fullCoverageCount;
		@XmlElement
		protected final long partialCoverageCount;
		@XmlElement
		protected final long noCoverageCount;
		@XmlElement
		protected final double emptyPercentage;
		@XmlElement
		protected final double fullCoveragePercentage;
		@XmlElement
		protected final double partialCoveragePercentage;
		@XmlElement
		protected final double noCoveragePercentage;

		protected LineStatistics(long totalCount, long emptyCount, long fullCoverageCount, long partialCoverageCount,
				long noCoverageCount) {
			super();
			this.totalCount = totalCount;
			this.emptyCount = emptyCount;
			this.fullCoverageCount = fullCoverageCount;
			this.partialCoverageCount = partialCoverageCount;
			this.noCoverageCount = noCoverageCount;

			if (totalCount == 0) {
				this.emptyPercentage = 0;
				this.fullCoveragePercentage = 0;
				this.partialCoveragePercentage = 0;
				this.noCoveragePercentage = 0;
			} else {
				this.emptyPercentage = (emptyCount * 100f) / totalCount;
				this.fullCoveragePercentage = (fullCoverageCount * 100f) / totalCount;
				this.partialCoveragePercentage = (partialCoverageCount * 100f) / totalCount;
				this.noCoveragePercentage = (noCoverageCount * 100f) / totalCount;
			}

		}

		@Override
		public int hashCode() {
			return Objects.hash(this.emptyCount, this.emptyPercentage, this.fullCoverageCount,
					this.fullCoveragePercentage, this.noCoverageCount,
					this.noCoveragePercentage, this.partialCoverageCount, this.partialCoveragePercentage,
					this.totalCount);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LineStatistics)) {
				return false;
			}
			final LineStatistics other = (LineStatistics) obj;
			return this.emptyCount == other.emptyCount
					&& Double.doubleToLongBits(this.emptyPercentage) == Double.doubleToLongBits(other.emptyPercentage)
					&& this.fullCoverageCount == other.fullCoverageCount
					&& Double.doubleToLongBits(this.fullCoveragePercentage) == Double
						.doubleToLongBits(other.fullCoveragePercentage)
					&& this.noCoverageCount == other.noCoverageCount
					&& Double.doubleToLongBits(this.noCoveragePercentage) == Double
						.doubleToLongBits(other.noCoveragePercentage)
					&& this.partialCoverageCount == other.partialCoverageCount
					&& Double.doubleToLongBits(this.partialCoveragePercentage) == Double
						.doubleToLongBits(other.partialCoveragePercentage)
					&& this.totalCount == other.totalCount;
		}
	}

}
