package org.x2vc.xml.value;

import org.x2vc.schema.ISchemaManager;
import org.x2vc.xml.request.IDocumentRequest;

import com.github.racc.tscg.TypesafeConfig;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IValueGeneratorFactory}.
 */
public class ValueGeneratorFactory implements IValueGeneratorFactory {

	private ISchemaManager schemaManager;
	private IPrefixSelector prefixSelector;
	private double discreteValueSelectionRatio;
	private int stringMinWordCount;
	private int stringMaxWordCount;

	/**
	 * @param prefixSelector
	 */
	@Inject
	ValueGeneratorFactory(ISchemaManager schemaManager, IPrefixSelector prefixSelector,
			@TypesafeConfig("x2vc.xml.value.discrete_value_selection_ratio") Double discreteValueSelectionRatio,
			@TypesafeConfig("x2vc.xml.value.string_min_word_count") Integer stringMinWordCount,
			@TypesafeConfig("x2vc.xml.value.string_max_word_count") Integer stringMaxWordCount) {

		super();
		this.schemaManager = schemaManager;
		this.prefixSelector = prefixSelector;
		this.discreteValueSelectionRatio = discreteValueSelectionRatio;
		this.stringMinWordCount = stringMinWordCount;
		this.stringMaxWordCount = stringMaxWordCount;
	}

	@Override
	public IValueGenerator createValueGenerator(IDocumentRequest request) {
		return new ValueGenerator(this.schemaManager, this.prefixSelector, request, this.discreteValueSelectionRatio,
				this.stringMinWordCount, this.stringMaxWordCount);
	}

}
