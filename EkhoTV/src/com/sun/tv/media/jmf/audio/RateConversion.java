/*
 * @(#)RateConversion.java	1.4 98/12/10
 *
 *  Licensed Materials - Property of IBM
 *  "Restricted Materials of IBM"
 *  5648-B81
 *  (c) Copyright IBM Corporation 1997,1998 All Rights Reserved
 *  US Government Users Restricted Rights - Use, duplication or
 *  disclosure restricted by GSA ADP Schedule Contract with
 *  IBM Corporation.
 *
 */

package com.sun.tv.media.jmf.audio;


/**
 * Rate conversion module (converts the input audio to 8 Khz Mu-Law mono)
 * 
 * @author Doron Hoffman
 * @version 1.1 , 24.11.98
 */

public final class RateConversion {

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1998.";

    /** The method was processed without errors */
    public static final int RATE_CONVERSION_OK = -1;
    /** The rate conversion ratio isn't supported */
    public static final int RATE_CONVERSION_NOT_SUPPORTED = -2;
    /** The rate conversion class was not initialized */
    public static final int RATE_CONVERSION_NOT_INITIALIZED = -3;
    /** The method received illegal parameter */
    public static final int RATE_CONVERSION_ILLEGAL_PARAMETER = -4;

    public static final int RATE_CONVERSION_RECOMMENDED_INPUT_SIZE = 1056;
    public static final int RATE_CONVERSION_MAX_SUPPORTED_CHANNELS = 2;
    public static final int RATE_CONVERSION_MAX_OUTPUT_FACTOR = (11 / 2);

    public static final int RATE_CONVERSION_BIG_ENDIAN_FORMAT = 0;
    public static final int RATE_CONVERSION_LITTLE_ENDIAN_FORMAT = 1;
    public static final int RATE_CONVERSION_BYTE_FORMAT = 2;

    // a flag to indicate dc removal of the input
    private static final boolean USE_REMOVE_DC = false;
    // a flag to indicate Mu-Law conversion at the output
    private static final boolean USE_MU_LAW_CONVERSION = true;

    private static final int MAX_RATE_IN = 11;
    private static final int UNROLLING_ORDER = 4;
    private static final int CORRECTION_FRAME_SIZE = 441;

    private static final int CONV_ERROR = -1;
    private static final int CONV_11to2 = 1;
    private static final int CONV_11to4 = 2;
    private static final int CONV_11to8 = 3;
    private static final int CONV_6to1 = 4;
    private static final int CONV_4to1 = 5;
    private static final int CONV_2to1 = 6;

    private static final int MAX_MEM_SIZE = 792;
    private static final float DCFACT = (127.0f / 128.0f);
    private static final float SHRT_MIN = -32767F;
    private static final float SHRT_MAX = +32767F;

    private static final float FRACTION_DELTA = (1.0F / 440.0F);
    private static final float FRACTION_DELIMITER = (1.0F + FRACTION_DELTA / 2.0F);

    private int inputBias;
    private int signMask;
    private int filterHistoryLength;
    private int decimFlag;

    private int numberOfInputChannels;
    private int numberOfOutputChannels;
    private int rateIn;
    private int rateOut;

    private int[] index = new int[RATE_CONVERSION_MAX_SUPPORTED_CHANNELS];

    /*
     * number of remained samples that should be processed during the next
     * process
     */
    private int inputRemainedSamples;

    /* maximum input length for one processing */
    private int maxInputLength;

    /* the amount of zero padding at drain */
    private int paddingLength;

    private int maxDrainedSamples;

    private int pcmType;
    private int inputSampleSize;

    // polyphase coefficients
    private float[] poly;

    // Input
    private float[] x1;
    private float[] x2;

    // Output
    private float[] y1;
    private float[] y2;

    private boolean needInputCorrection;
    private boolean isDrained;
    private boolean isRateConversionInited;

    // remove delay of filter
    private int delay;

    // IIR DC removal
    private float lastInputSample1;
    private float lastInputSample2;

    // linear correction variables
    private float frac;
    private float prev_sample1;
    private float prev_sample2;

    private float fractionDelta = 1.0f / 440.0f;
    private float fractionDelimiter;
    private int precisionCountDelimiter = 1;
    private int precisionCount;

    /**
     * Init the rate conversion object.
     * 
     * @param maxInputBufferSize
     *            Maximum number of samples which will be processed at one
     *            process method call
     * @param rateInput
     *            input rate
     * @param rateOutput
     *            output rate
     * @param inputChannels
     *            number of input channels
     * @param outputChannels
     *            number of output channels
     * @param pcmType
     *            pcm type <br>
     *            (RATE_CONVERSION_BIG_ENDIAN_FORMAT,
     *            RATE_CONVERSION_LITTLE_ENDIAN_FORMAT
     *            ,RATE_CONVERSION_BYTE_FORMAT)
     * @param signed
     *            flag that indicates if the sample is signed
     * 
     * 
     * @return RATE_CONVERSION_OK or <br>
     *         RATE_CONVERSION_NOT_SUPPORTED or <br>
     *         RATE_CONVERSION_ILLEGAL_PARAMETER
     */

    public int init(int maxInputBufferSize, int rateInput, int rateOutput,
	    int inputChannels, int outputChannels, int pcmType, boolean signed) {

	int i, j;
	float gain;
	int filterLength;

	inputSampleSize = 2;

	if ((RATE_CONVERSION_BIG_ENDIAN_FORMAT != pcmType)
		&& (RATE_CONVERSION_LITTLE_ENDIAN_FORMAT != pcmType)
		&& (RATE_CONVERSION_BYTE_FORMAT != pcmType)) {
	    return RATE_CONVERSION_ILLEGAL_PARAMETER;
	}

	if (2 == numberOfOutputChannels) {
	    return RATE_CONVERSION_ILLEGAL_PARAMETER;
	}

	if (signed) {
	    inputBias = 0;
	    signMask = 0xffffffff;
	} else {
	    inputBias = 32768;
	    signMask = 0x0000ffff;
	}

	if (RATE_CONVERSION_BYTE_FORMAT == pcmType) {
	    inputSampleSize = 1;
	}

	this.pcmType = pcmType;
	maxInputLength = maxInputBufferSize / (inputSampleSize * inputChannels);
	numberOfInputChannels = inputChannels;
	numberOfOutputChannels = outputChannels;

	needInputCorrection = false;
	delay = 0;
	decimFlag = CONV_ERROR;

	/*
	 * find conversion ratio flag
	 */

	if ((rateInput == 44100) && (rateOutput == 8000)) {
	    decimFlag = CONV_11to2;
	    rateIn = 11;
	    rateOut = 2;
	    needInputCorrection = true;
	} else if ((rateInput == 22050) && (rateOutput == 8000)) {
	    decimFlag = CONV_11to4;
	    rateIn = 11;
	    rateOut = 4;
	    needInputCorrection = true;
	} else if ((rateInput == 11025) && (rateOutput == 8000)) {
	    decimFlag = CONV_11to8;
	    rateIn = 11;
	    rateOut = 8;
	    needInputCorrection = true;
	} else if ((rateInput == 48000) && (rateOutput == 8000)) {
	    decimFlag = CONV_6to1;
	    rateIn = 6;
	    rateOut = 1;
	    needInputCorrection = false;
	} else if ((rateInput == 32000) && (rateOutput == 8000)) {
	    decimFlag = CONV_4to1;
	    rateIn = 4;
	    rateOut = 1;
	    needInputCorrection = false;
	} else if ((rateInput == 16000) && (rateOutput == 8000)) {
	    decimFlag = CONV_2to1;
	    rateIn = 2;
	    rateOut = 1;
	    needInputCorrection = false;
	}

	else if ((rateInput == 11127) && (rateOutput == 8000)) {
	    decimFlag = CONV_11to8;
	    rateIn = 11;
	    rateOut = 8;
	    needInputCorrection = true;
	    fractionDelta = 127.0f / 11000.0f;
	    precisionCountDelimiter = 127;
	}

	else if ((rateInput == 22254) && (rateOutput == 8000)) {
	    decimFlag = CONV_11to4;
	    rateIn = 11;
	    rateOut = 4;
	    needInputCorrection = true;

	    fractionDelta = 127.0f / 11000.0f;
	    precisionCountDelimiter = 127;

	}

	else if ((rateInput == 22255) && (rateOutput == 8000)) {
	    decimFlag = CONV_11to4;
	    rateIn = 11;
	    rateOut = 4;
	    needInputCorrection = true;

	    fractionDelta = 255.0f / 22000.0f;
	    precisionCountDelimiter = 255;

	}

	else {
	    close();
	    return RATE_CONVERSION_NOT_SUPPORTED;
	}

	/*
	 * compute the necessary input signal padding to fill up the filter
	 * memory and the output signal length
	 */

	switch (decimFlag) {

	case CONV_11to8:

	    filterHistoryLength = RateConversionTables.POLY_11_TO_8_LEN;
	    delay = (int) (0.5f + (RateConversionTables.FILTER11_LEN - 1.f)
		    / 2.f * (1.f / 11.f));
	    filterLength = RateConversionTables.FILTER11_LEN;
	    break;

	case CONV_11to4:

	    filterHistoryLength = RateConversionTables.POLY_11_TO_4_LEN;
	    delay = (int) (0.5f + (RateConversionTables.FILTER11_LEN - 1.f)
		    / 2.f * (1.f / 11.f));
	    filterLength = RateConversionTables.FILTER11_LEN;
	    break;

	case CONV_11to2:

	    filterHistoryLength = RateConversionTables.POLY_11_TO_2_LEN;
	    delay = (int) (0.5f + (RateConversionTables.FILTER11_LEN - 1.f)
		    / 2.f * (1.f / 11.f));
	    filterLength = RateConversionTables.FILTER11_LEN;
	    break;

	case CONV_6to1:
	    filterHistoryLength = RateConversionTables.FILTER6_LEN;
	    delay = (int) (0.5f + (RateConversionTables.FILTER6_LEN - 1.f)
		    / 2.f * (1.f / 6.f));
	    filterLength = RateConversionTables.FILTER6_LEN;
	    break;

	case CONV_4to1:
	    filterHistoryLength = RateConversionTables.FILTER4_LEN;
	    delay = (int) (0.5f + (RateConversionTables.FILTER4_LEN - 1.f)
		    / 2.f * (1.f / 4.f));
	    filterLength = RateConversionTables.FILTER4_LEN;
	    break;

	case CONV_2to1:
	    filterHistoryLength = RateConversionTables.FILTER2_LEN;
	    delay = (int) (0.5f + (RateConversionTables.FILTER2_LEN - 1.f)
		    / 2.f * (1.f / 2.f));
	    filterLength = RateConversionTables.FILTER2_LEN;
	    break;

	default:
	    close();
	    return RATE_CONVERSION_NOT_SUPPORTED;

	}

	poly = new float[filterLength];
	/*
	 * x1 = new
	 * float[maxInputBufferSize+filterHistoryLength+UNROLLING_ORDER*rateIn];
	 * y1 = new
	 * float[(maxInputBufferSize+filterHistoryLength+UNROLLING_ORDER
	 * *rateIn)/rateIn*rateOut];
	 */

	x1 = new float[maxInputLength + filterHistoryLength + UNROLLING_ORDER
		* rateIn];
	y1 = new float[(maxInputLength + filterHistoryLength + UNROLLING_ORDER
		* rateIn)
		/ rateIn * rateOut];

	/*
	 * fix filter gain: usually 1 for decimator, and the interpolation-ratio
	 * for interpolator
	 * 
	 * prepare polyphase filters
	 */

	gain = (float) 1.;
	switch (decimFlag) {

	case CONV_11to2:
	    gain = (float) 2.0;
	    for (i = 0; i < 2; i++)
		for (j = 0; j < RateConversionTables.POLY_11_TO_2_LEN; j++)
		    poly[i * RateConversionTables.POLY_11_TO_2_LEN + j] = RateConversionTables.filter11[i
			    + j * 2]
			    * gain;
	    break;

	case CONV_11to4:
	    gain = (float) 4.0;
	    for (i = 0; i < 4; i++)
		for (j = 0; j < RateConversionTables.POLY_11_TO_4_LEN; j++)
		    poly[i * RateConversionTables.POLY_11_TO_4_LEN + j] = RateConversionTables.filter11[i
			    + j * 4]
			    * gain;
	    break;

	case CONV_11to8:
	    gain = (float) 8.0;
	    for (i = 0; i < 8; i++)
		for (j = 0; j < RateConversionTables.POLY_11_TO_8_LEN; j++)
		    poly[i * RateConversionTables.POLY_11_TO_8_LEN + j] = RateConversionTables.filter11[i
			    + j * 8]
			    * gain;
	    break;

	/*
	 * case CONV_6to1: for ( i = 0; i < 6; i++ ) for ( j = 0; j <
	 * RateConversionTables.POLY_6_TO_1_LEN; j++ )
	 * poly[i*RateConversionTables
	 * .POLY_6_TO_1_LEN+RateConversionTables.POLY_6_TO_1_LEN - 1 - j] =
	 * RateConversionTables.filter6[(5-i) + j * 6]; break;
	 * 
	 * case CONV_4to1: for ( i = 0; i < 4; i++ ) for ( j = 0; j <
	 * RateConversionTables.POLY_4_TO_1_LEN; j++ )
	 * poly[i*RateConversionTables
	 * .POLY_4_TO_1_LEN+RateConversionTables.POLY_4_TO_1_LEN - 1 - j] =
	 * RateConversionTables.filter4[(3-i) + j * 4]; break;
	 * 
	 * case CONV_2to1: for ( i = 0; i < 2; i++ ) for ( j = 0; j <
	 * RateConversionTables.POLY_2_TO_1_LEN; j++ )
	 * poly[i*RateConversionTables
	 * .POLY_2_TO_1_LEN+RateConversionTables.POLY_2_TO_1_LEN - 1 - j] =
	 * RateConversionTables.filter2[(1-i) + j * 2]; break;
	 */

	case CONV_6to1:
	    for (i = 0; i < RateConversionTables.FILTER6_LEN; i++)
		poly[i] = RateConversionTables.filter6[i];
	    break;

	case CONV_4to1:
	    for (i = 0; i < RateConversionTables.FILTER4_LEN; i++)
		poly[i] = RateConversionTables.filter4[i];
	    break;

	case CONV_2to1:
	    for (i = 0; i < RateConversionTables.FILTER2_LEN; i++)
		poly[i] = RateConversionTables.filter2[i];
	    break;
	}

	paddingLength = filterHistoryLength / 2;

	// max drained samples includes padding
	maxDrainedSamples = (int) ((float) ((this.paddingLength + UNROLLING_ORDER
		* rateIn * 2) * rateOut) / (float) rateIn);
	// maxInputLength = maxInputBufferSize;

	isRateConversionInited = true;
	fractionDelimiter = 1.0f + fractionDelta / 2.0f;
	reset();

	return RATE_CONVERSION_OK;
    }

    /**
     * reset the rate conversion object.
     */

    public int reset() {

	int i;

	if (false == isRateConversionInited)
	    return RATE_CONVERSION_NOT_INITIALIZED;

	inputRemainedSamples = 0;
	isDrained = false;

	frac = 1.0F;
	precisionCount = precisionCountDelimiter - 1;
	prev_sample1 = 0.0F;
	prev_sample2 = 0.0F;

	/*
	 * reset signal memory
	 */
	for (i = 0; i < filterHistoryLength; i++)
	    x1[i] = (float) 0.0;

	index[0] = 0;

	/*
	 * if (numberOfOutputChannels == 2) { for ( i = 0; i <
	 * filterHistoryLength; i++ ) x2[i] = (float)0.0; index[1] = 0; }
	 */

	/* Init DC removal IIR memroy */
	lastInputSample1 = (float) 0.;
	lastInputSample2 = (float) 0.;

	return RATE_CONVERSION_OK;
    }

    /**
     * close the rate conversion object.
     */

    public void close() {

	isRateConversionInited = false;

	x1 = null;
	x2 = null;
	y1 = null;
	y2 = null;
	poly = null;

    }

    /**
     * get the algorithmic delay
     * 
     * @return delay
     */

    public int getDelay() {

	if (false == isRateConversionInited)
	    return RATE_CONVERSION_NOT_INITIALIZED;

	int outputDelayLength = delay;

	if (false == USE_MU_LAW_CONVERSION) {
	    outputDelayLength *= 2;
	}

	return outputDelayLength;
    }

    /**
     * convert the input buffer using the rate conversion module (as defined in
     * the init parameters)
     * 
     * @param inputData
     *            buffer of input samples
     * @param inputDataOffset
     *            offset of the input buffer
     * @param inputDataLength
     *            number of bytes that should be processed within the input
     *            buffer
     * @param output
     *            output buffer
     * @param outputDataOffset
     *            offset of the output buffer
     * 
     * @return output length (in bytes) or <br>
     *         RATE_CONVERSION_NOT_INITIALIZED or <br>
     *         RATE_CONVERSION_ILLEGAL_PARAMETER
     */

    public int process(byte[] inputData, int inputDataOffset,
	    int inputDataLength, byte[] output, int outputDataOffset) {

	int inputLength, outputLength;
	int i, j, itmp, len;
	float[] x;
	float[] y;
	int inputBlocks;
	byte[] input;
	int inputOffset;

	if (false == isRateConversionInited)
	    return RATE_CONVERSION_NOT_INITIALIZED;

	if ((false == isDrained)
		&& (inputDataLength > (maxInputLength * inputSampleSize * numberOfInputChannels))) {
	    // return RATE_CONVERSION_ILLEGAL_PARAMETER;
	    enlargeBufferAllocation(inputDataLength);
	}

	if (0 == inputDataLength)
	    return 0;

	inputOffset = inputRemainedSamples + filterHistoryLength;

	inputDataLength = extractInput(inputData, inputDataOffset,
		inputDataLength, inputOffset);

	if (USE_REMOVE_DC) {
	    lastInputSample1 = remove_dc(x1, inputOffset, lastInputSample1,
		    inputDataLength);
	    /*
	     * if (2 == numberOfOutputChannels) { lastInputSample2 =
	     * remove_dc(x2,inputOffset,lastInputSample2,inputDataLength); }
	     */
	}

	inputLength = inputDataLength + inputRemainedSamples;
	inputBlocks = inputLength / (UNROLLING_ORDER * rateIn);
	inputRemainedSamples = inputLength - inputBlocks
		* (UNROLLING_ORDER * rateIn);
	inputLength -= inputRemainedSamples;

	inputOffset = inputRemainedSamples + filterHistoryLength;

	if (inputLength == 0) {
	    return 0;
	}

	outputLength = inputLength / rateIn * rateOut;

	/* call for interpolation/decimation */
	x = x1;
	y = y1;
	i = 0;

	/*
	 * for ( i = 0 ; i < numberOfOutputChannels ; i++ ) {
	 * 
	 * if (i > 0) { x = x2; y = y2; } else { x = x1; y = y1; }
	 */

	/*
	 * if UNROLLING_ORDER is changed change all (>> log2(UNROLLING_ORDER) )
	 * in input_block_size , output_block_size parameters calls to
	 * upsampleMtoL , upsampleL , downsampleMtoL , downsampleM functions
	 */

	switch (decimFlag) {

	case CONV_11to8:
	    index[i] = downsampleMtoL(x, y, index[i], poly,
		    RateConversionTables.POLY_11_TO_8_LEN, 8, 11,
		    (outputLength >> 5) * 11, (outputLength >> 2));
	    break;

	case CONV_11to4:
	    index[i] = downsampleMtoL(x, y, index[i], poly,
		    RateConversionTables.POLY_11_TO_4_LEN, 4, 11,
		    (outputLength >> 4) * 11, (outputLength >> 2));
	    break;

	case CONV_11to2:
	    index[i] = downsampleMtoL(x, y, index[i], poly,
		    RateConversionTables.POLY_11_TO_2_LEN, 2, 11,
		    (outputLength >> 3) * 11, (outputLength >> 2));
	    break;

	/*
	 * case CONV_6to1: index[i] =
	 * downsampleM(x,y,index[i],poly,RateConversionTables
	 * .POLY_6_TO_1_LEN,6,(outputLength >> 2)*6,(outputLength >> 2)); break;
	 * 
	 * case CONV_4to1: index[i] =
	 * downsampleM(x,y,index[i],poly,RateConversionTables
	 * .POLY_4_TO_1_LEN,4,outputLength,(outputLength >> 2)); break;
	 * 
	 * case CONV_2to1: index[i] =
	 * downsampleM(x,y,index[i],poly,RateConversionTables
	 * .POLY_2_TO_1_LEN,2,(outputLength >> 1),(outputLength >> 2)); break;
	 */

	case CONV_6to1:
	    downsampleM(x, y, poly, RateConversionTables.FILTER6_LEN, 6,
		    outputLength);
	    break;

	case CONV_4to1:
	    downsampleM(x, y, poly, RateConversionTables.FILTER4_LEN, 4,
		    outputLength);
	    break;

	case CONV_2to1:
	    downsampleM(x, y, poly, RateConversionTables.FILTER2_LEN, 2,
		    outputLength);
	    break;

	}

	/* save last input samples */
	for (j = 0; j < inputOffset; j++)
	    x[j] = x[j + inputLength];

	// }

	/*
	 * Convert Float Result to Short
	 */

	if (false == USE_MU_LAW_CONVERSION) {
	    // if (numberOfOutputChannels == 1) {
	    Fl2Byte(y1, outputLength, output, outputDataOffset);
	    return 2 * outputLength; // 2 bytes per sample
	    // }
	    /*
	     * else { Fl2ByteStereo(y1,y2,outputLength,output,outputDataOffset);
	     * }
	     */
	} else {
	    convertToMuLaw(y1, outputLength, output, outputDataOffset);
	    return outputLength; // 1 byte per sample
	}

    }

    // private final byte[] inputTmp = new
    // byte[(MAX_MEM_SIZE+UNROLLING_ORDER*MAX_RATE_IN)*RATE_CONVERSION_MAX_SUPPORTED_CHANNELS*2];

    /**
     * drain the internal buffers
     * 
     * @param output
     *            output buffer
     * @param outputOffset
     *            offset of the output buffer
     * 
     * @return output length (in bytes) or <br>
     *         RATE_CONVERSION_NOT_INITIALIZED or <br>
     */

    public int drain(byte[] output, int outputOffset) {

	// byte[] input=inputTmp;
	int inputSamples;
	int numberOfOutputSamples;
	int actualOutputSamples;

	if (false == isRateConversionInited)
	    return RATE_CONVERSION_NOT_INITIALIZED;

	/* Zero Buffer */
	inputSamples = (paddingLength + UNROLLING_ORDER * rateIn)
		* numberOfInputChannels * inputSampleSize; /* pad the input data */

	/*
	 * for(int i=0;i<inputSamples;i++) input[i] = 0;
	 */

	isDrained = true;

	actualOutputSamples = (int) ((float) ((paddingLength + inputRemainedSamples) * rateOut) / (float) rateIn);

	if (false == USE_MU_LAW_CONVERSION) {
	    actualOutputSamples *= 2;
	}

	numberOfOutputSamples = process(null, 0, inputSamples, output,
		outputOffset);

	isDrained = false;

	if (actualOutputSamples < numberOfOutputSamples)
	    numberOfOutputSamples = actualOutputSamples;

	return numberOfOutputSamples;
    }

    /**
     * get the maximum output length (based on the given maximum input length
     * given in the init method)
     * 
     * @return output length (in bytes) or <br>
     *         RATE_CONVERSION_NOT_INITIALIZED or <br>
     */

    public int getOutputLength() {

	if (false == isRateConversionInited)
	    return RATE_CONVERSION_NOT_INITIALIZED;

	// The number of output samples
	int outputLength = ((maxInputLength + rateIn * UNROLLING_ORDER)
		* rateOut / rateIn);

	if (false == USE_MU_LAW_CONVERSION) {
	    outputLength *= 2;
	}

	return outputLength;
    }

    /**
     * get the maximum drain length (useful to verify the output buffer size
     * before calling drain method)
     * 
     * @return max drain length (in bytes) or <br>
     *         RATE_CONVERSION_NOT_INITIALIZED or <br>
     */

    public int getDrainMaxLength() {

	if (false == isRateConversionInited)
	    return RATE_CONVERSION_NOT_INITIALIZED;

	// The max number of drain output samples
	int drainMaxLength = maxDrainedSamples;

	if (false == USE_MU_LAW_CONVERSION) {
	    drainMaxLength *= 2;
	}

	return drainMaxLength;
    }

    private void convertToMuLaw(float[] inBuffer, int Len, byte[] outBuffer,
	    int indexOut) {
	if (true == USE_MU_LAW_CONVERSION) {
	    int sample, signBit;
	    for (int i = 0; i < Len; i++) {
		float inSample = inBuffer[i];

		if (inSample < SHRT_MIN)
		    inSample = SHRT_MIN;
		else if (inSample > SHRT_MAX)
		    inSample = SHRT_MAX;

		sample = (int) inSample;

		if (sample >= 0) { // Sample=abs(sample)
		    signBit = 0x80; // sign bit
		} else {
		    sample = -sample;
		    signBit = 0x00;
		}
		sample = (132 + sample) >> 3; // bias

		if (sample < 0x20) {
		    outBuffer[indexOut++] = (byte) (signBit | (7 << 4) | (31 - (sample >> 0)));
		} else if (sample < 0x0040) {
		    outBuffer[indexOut++] = (byte) (signBit | (6 << 4) | (31 - (sample >> 1)));
		} else if (sample < 0x0080) {
		    outBuffer[indexOut++] = (byte) (signBit | (5 << 4) | (31 - (sample >> 2)));
		} else if (sample < 0x0100) {
		    outBuffer[indexOut++] = (byte) (signBit | (4 << 4) | (31 - (sample >> 3)));
		} else if (sample < 0x0200) {
		    outBuffer[indexOut++] = (byte) (signBit | (3 << 4) | (31 - (sample >> 4)));
		} else if (sample < 0x0400) {
		    outBuffer[indexOut++] = (byte) (signBit | (2 << 4) | (31 - (sample >> 5)));
		} else if (sample < 0x0800) {
		    outBuffer[indexOut++] = (byte) (signBit | (1 << 4) | (31 - (sample >> 6)));
		} else if (sample < 0x1000) {
		    outBuffer[indexOut++] = (byte) (signBit | (0 << 4) | (31 - (sample >> 7)));
		} else {
		    outBuffer[indexOut++] = (byte) (signBit | (0 << 4) | (31 - (0xfff >> 7)));
		}
	    }

	}

    }

    private final void Fl2Byte(float[] inBuffer, int Len, byte[] outBuffer,
	    int indexOut) {
	int j = 0; // Output bytes count

	if (false == USE_MU_LAW_CONVERSION) {
	    for (int i = 0; i < Len; i++) {
		float sample = inBuffer[i];

		if (sample < SHRT_MIN)
		    sample = SHRT_MIN;
		else if (sample > SHRT_MAX)
		    sample = SHRT_MAX;

		/* 16 bit representation */
		int TempInt = (int) sample;

		outBuffer[indexOut + j] = (byte) (TempInt & 0x00ff);
		outBuffer[indexOut + j + 1] = (byte) (TempInt >> 8);

		j += 2; // Two bytes written

	    }
	}
    }

    /*
     * private final void Fl2ByteStereo(float[] inBuffer1,float[] inBuffer2,int
     * Len, byte[] outBuffer,int indexOut) { int j=0; // Output bytes count
     * 
     * if (false == USE_MU_LAW_CONVERSION) { for (int i=0; i<Len; i+=2) { float
     * sample = inBuffer1[i];
     * 
     * if (sample < SHRT_MIN) sample = SHRT_MIN ; else if (sample > SHRT_MAX)
     * sample = SHRT_MAX ;
     * 
     * int TempInt = (int)sample;
     * 
     * outBuffer[indexOut+j] = (byte)(TempInt & 0x00ff); outBuffer[indexOut+j+1]
     * = (byte)(TempInt >> 8 );
     * 
     * sample = inBuffer2[i+1];
     * 
     * if (sample < SHRT_MIN) sample = SHRT_MIN ; else if (sample > SHRT_MAX)
     * sample = SHRT_MAX ;
     * 
     * 
     * TempInt = (int)sample;
     * 
     * 
     * outBuffer[indexOut+j+2] = (byte)(TempInt & 0x00ff);
     * outBuffer[indexOut+j+3] = (byte)(TempInt >> 8 );
     * 
     * j +=4; // 4 bytes written
     * 
     * } } }
     */

    private int extractInput(byte[] input, int inputOffset, int inputLength,
	    int internalBufferOffset) {

	int internalBufferIndex = internalBufferOffset;
	float samples1 = 0.0F;
	float samples2 = 0.0F;
	int inputSample = 0;
	int i;
	int lsbOffset;
	int msbOffset;

	if (isDrained) {
	    int length = inputLength
		    / (numberOfInputChannels * inputSampleSize);
	    for (i = 0; i < length; i++) {
		x1[internalBufferIndex + i] = 0.0F;
	    }

	    return length;
	}

	if (pcmType == RATE_CONVERSION_LITTLE_ENDIAN_FORMAT) {
	    lsbOffset = -1;
	    msbOffset = 1;
	} else {
	    lsbOffset = 1;
	    msbOffset = 0;

	}

	for (i = inputOffset + msbOffset; i < (inputLength + inputOffset);) {

	    if (1 == inputSampleSize) {
		inputSample = input[i++] << 8;

		if (2 == numberOfInputChannels) {
		    inputSample = ((inputSample & signMask) + ((input[i++] << 8) & signMask)) >> 1;
		}
	    } else {
		inputSample = (input[i] << 8) + (0xff & input[i + lsbOffset]);
		i += 2;

		if (2 == numberOfInputChannels) {
		    inputSample = ((inputSample & signMask) + (((input[i] << 8) + (0xff & input[i
			    + lsbOffset])) & signMask)) >> 1;
		    i += 2;
		}
	    }

	    samples1 = (float) ((short) (inputSample + inputBias));

	    if (needInputCorrection) {

		if (frac > fractionDelimiter) {
		    precisionCount++;

		    if (precisionCount == precisionCountDelimiter) {
			precisionCount = 0;
			frac = fractionDelta;

		    } else {
			frac -= 1.0f;
		    }

		    prev_sample1 = samples1;
		    // prev_sample2 = samples2;
		    continue;
		}

		x1[internalBufferIndex] = prev_sample1 * (1.0f - frac) + frac
			* samples1;
		prev_sample1 = samples1;

		/*
		 * if (2 == numberOfOutputChannels) { x2[internalBufferIndex] =
		 * prev_sample2*(1.0f-frac) + frac*samples2; prev_sample2 =
		 * samples2; }
		 */

		frac += fractionDelta;
	    } else {
		x1[internalBufferIndex] = samples1;

		/*
		 * if (2 == output_channels) { x2[internalBufferIndex] =
		 * samples2; }
		 */

	    }

	    internalBufferIndex++;
	}

	return internalBufferIndex - internalBufferOffset;

    }

    private float remove_dc(float[] input, int inputOffset,
	    float previous_sample, int length) {
	int i;
	float new_sample;

	if (USE_REMOVE_DC) {
	    for (i = 0; i < length; i++) {

		new_sample = input[inputOffset + i] - previous_sample + DCFACT
			* input[inputOffset + i - 1];

		previous_sample = input[inputOffset + i];

		input[inputOffset + i] = new_sample;
	    }

	    return previous_sample;
	} else {
	    return (float) 0.;
	}

    }

    private int downsampleMtoL(float[] x, /* input signal */
	    float[] y, /* output signal */
	    int index, /* polyphase running index */
	    float[] poly, /* polyphase coefficients */
	    int poly_length, int interpolation_factor, int decimation_factor,
	    int input_block_size, int output_block_size)

    {
	int i, j, m, n;
	float sum0, sum1, sum2, sum3;
	int offset;
	float polySample;

	m = 0;
	for (n = 0; n < output_block_size; n++) {

	    sum0 = sum1 = sum2 = sum3 = (float) 0.0;
	    offset = index * poly_length;
	    for (j = 0; j < poly_length; j++) {
		polySample = poly[offset + j];
		sum0 += polySample * x[j + m];
		sum1 += polySample * x[j + m + 1 * input_block_size];
		sum2 += polySample * x[j + m + 2 * input_block_size];
		sum3 += polySample * x[j + m + 3 * input_block_size];

	    }
	    y[n + 1 * output_block_size] = sum1;
	    y[n + 2 * output_block_size] = sum2;
	    y[n + 3 * output_block_size] = sum3;
	    y[n] = sum0;

	    for (; index < decimation_factor; index += interpolation_factor, m++)
		;
	    index -= decimation_factor;
	}

	return (index);
    }

    void downsampleM(float[] x, float[] y, float[] filter, int filter_length,
	    int decimation_factor, int output_length) {

	int i, j, k, n;
	float filter_sample;
	float sum0, sum1, sum2, sum3;
	int filt_length = filter_length / 2; // filtering length
	int index1; // input index
	int index2; // symmetric input index position
	int offset_inc = decimation_factor * UNROLLING_ORDER;
	int offset1, offset2, offset3, offset4;
	float filterSample;
	int sym_offset = filter_length - 1;

	for (offset1 = 0, n = 0; n < output_length; offset1 += offset_inc) {
	    sum0 = sum1 = sum2 = sum3 = 0.0F;
	    offset2 = offset1 + decimation_factor;
	    offset3 = offset2 + decimation_factor;
	    offset4 = offset3 + decimation_factor;

	    for (i = 0; i < filt_length; i++) {

		filterSample = filter[i];
		sum0 += filterSample
			* (x[offset1 + i] + x[offset1 + sym_offset - i]);
		sum1 += filterSample
			* (x[offset2 + i] + x[offset2 + sym_offset - i]);
		sum2 += filterSample
			* (x[offset3 + i] + x[offset3 + sym_offset - i]);
		sum3 += filterSample
			* (x[offset4 + i] + x[offset4 + sym_offset - i]);

	    }

	    y[n++] = sum0;
	    y[n++] = sum1;
	    y[n++] = sum2;
	    y[n++] = sum3;

	}

    }

    private void enlargeBufferAllocation(int length) {

	float[] inputBuffer;
	float[] outputBuffer;
	int i;

	maxInputLength = length / (inputSampleSize * numberOfInputChannels);

	inputBuffer = new float[maxInputLength + filterHistoryLength
		+ UNROLLING_ORDER * rateIn];
	outputBuffer = new float[(maxInputLength + filterHistoryLength + UNROLLING_ORDER
		* rateIn)
		/ rateIn * rateOut];

	for (i = 0; i < x1.length; i++) {
	    inputBuffer[i] = x1[i];
	}

	for (i = 0; i < y1.length; i++) {
	    outputBuffer[i] = y1[i];
	}

	x1 = inputBuffer;
	y1 = outputBuffer;

    }

} // end of class: RateConversion {

