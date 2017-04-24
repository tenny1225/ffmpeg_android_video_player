
#include "FFVideoReader.cpp"
#include "FFAudioReader.cpp"
#include "common/Matrix.cpp"
#include <unistd.h>
#include "Thread.cpp"

const GLfloat rectanglePosition[] = {
        1, 1, 0,   // top right
        -1, 1, 0,  // top left
        -1, -1, 0, // bottom left
        1, -1, 0,  // bottom right
};
const GLshort VERTEX_INDEX[] = {0, 1, 2, 0, 2, 3};
const GLfloat UV_TEX_VERTEX[] = {   // in clockwise order:
        1, 0,  // bottom right
        0, 0,  // bottom left
        0, 1,  // top left
        1, 1,  // top right
};
static const char gVertexShader[] =
        {
                "attribute vec4 vPosition;\n"
                        "attribute vec2 a_texCoord;\n"
                        "varying vec2 v_texCoord;\n"
                        "uniform mat4 uMVPMatrix;\n"
                        "void main() {\n"
                        "  gl_Position = uMVPMatrix *vPosition;\n"
                        "  v_texCoord = a_texCoord;\n"
                        "}"

        };

static const char gFragmentShader[] =
        {
                "precision mediump float;\n"
                        "varying vec2 v_texCoord;\n"
                        "uniform sampler2D s_texture;\n"
                        "void main() {\n"
                        "  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
                        "}"
        };


GLuint gPositionHandle = 0;
GLuint mMatrixHandle;
GLuint gProgram = 0;
GLuint mTexCoordHandle;

GLuint mTexSamplerHandle;


GLuint mTexNames[1];

GLfloat mProjectionMatrix[16];
GLfloat  mCameraMatrix [16];
GLfloat mMVPMatrix[16];


GLuint loadShader(GLenum type, const char *source) {
    GLuint shader = glCreateShader(type);
    if (shader) {
        glShaderSource(shader, 1, &source, NULL);
        glCompileShader(shader);
        GLint compileStatus = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
        if (!compileStatus) {
            GLint info_length = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &info_length);
            if (info_length) {
                char *buf = (char *) malloc(info_length * sizeof(char));
                if (buf) {
                    glGetShaderInfoLog(shader, info_length, NULL, buf);
                    GLLOGS("Create shader %d failed\n%s\n", type, buf);
                }
            }
            glDeleteShader(shader);
            shader = 0;
        }
    }

    return shader;
}

GLuint createProgram(const char *pVertexSource, const char *pFragmentSource) {

    GLuint vshader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vshader) {
        return 0;
    }
    GLuint fshader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!fshader) {
        return 0;
    }
    GLuint program = glCreateProgram();


    if (program) {
        glAttachShader(program, vshader);

        glAttachShader(program, fshader);

        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char *buf = (char *) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

float w, h;
FFVideoReader ffVideoReader;
FrameInfor p;
FFAudioReader ffAudioReader;


//全局变量
 JavaVM *g_jvm = NULL;
 jobject g_obj = NULL;
jclass clazz;

void drawFrame() {
    glTexSubImage2D(GL_TEXTURE_2D,0,0,0,ffVideoReader._screenW, ffVideoReader._screenH,GL_RGB, GL_UNSIGNED_BYTE,p._data);


    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(gProgram);




    glEnableVertexAttribArray(gPositionHandle);
    glVertexAttribPointer(gPositionHandle, 3, GL_FLOAT, false,
                          0, rectanglePosition);
    glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix);


    glEnableVertexAttribArray(mTexCoordHandle);
    glVertexAttribPointer(mTexCoordHandle, 2, GL_FLOAT, false, 0,UV_TEX_VERTEX);
    glUniform1i(mTexSamplerHandle, 0);
    glDrawElements(GL_TRIANGLES, 6,
                   GL_UNSIGNED_SHORT, VERTEX_INDEX);

    glDisableVertexAttribArray(mTexCoordHandle);
    glDisableVertexAttribArray(gPositionHandle);



}
static time_t GetUtcCaressing()
{
    timeval tv;
    gettimeofday(&tv,NULL);
    return ((time_t)tv.tv_sec*(time_t)1000000+tv.tv_usec);
}



class VideoThread :public Thread{
public:
    virtual void run() override {



        JNIEnv *en;
        time_t timep1 =GetUtcCaressing();

        while(true){

            ffVideoReader.readFrame(p);
            if(p._data==0){
                break;
            }
            double d = p._timeBase*p._pts*1000*1000;
            g_jvm->AttachCurrentThread(&en, NULL);
            jclass clazz = en->GetObjectClass(g_obj);
            jmethodID mid =en->GetMethodID(clazz,"refresh","()V");
            en->CallObjectMethod(g_obj,mid);
            g_jvm->DetachCurrentThread();

            time_t timep2=GetUtcCaressing();


            double temp = timep2-timep1;
            double sleeps = d-temp;
            if(sleeps>1){
                usleep(sleeps);
            }

        }
    }
};

class AudioThread :public Thread{
public:

    virtual void run() override {
        ffAudioReader.play("/storage/emulated/0/sample.mp4");
    }
};
extern "C"
JNIEXPORT void JNICALL
Java_com_example_tenny_myapplication1_GLJniLib_init
        (JNIEnv *env, jobject jobj, jint width, jint height) {
    w = width;
    h = height;

    env->GetJavaVM(&g_jvm);

    g_obj = env->NewGlobalRef(jobj);


    ffVideoReader.load("/storage/emulated/0/sample.mp4");


    VideoThread videoThread;
    videoThread.start();

    AudioThread audioThread;
    audioThread.start();


    gProgram = createProgram(gVertexShader, gFragmentShader);


    gPositionHandle = glGetAttribLocation(gProgram, "vPosition");
    mMatrixHandle = glGetUniformLocation(gProgram, "uMVPMatrix");
    mTexCoordHandle = glGetAttribLocation(gProgram, "a_texCoord");
    mTexSamplerHandle = glGetUniformLocation(gProgram, "s_texture");


    glGenTextures(1, mTexNames);


    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTexNames[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, ffVideoReader._screenW, ffVideoReader._screenH, 0, GL_RGB, GL_UNSIGNED_BYTE,0);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                    GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                    GL_REPEAT);
    float ratio = (float) height / width;
    Matrix matrix;
    matrix.matrixFrustumM(mProjectionMatrix, -1, 1, -ratio, ratio, 3, 7);
    matrix.matrixLookAtM(mCameraMatrix, 0, 0, 3, 0, 0, 0, 0, 1, 0);
    matrix.matrixMultiplyMM(mMVPMatrix,  mProjectionMatrix, mCameraMatrix);
}

/*
 * Class:     com_example_tenny_myapplication1_OpenGLJniLib
 * Method:    create
 * Signature: ()V
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_tenny_myapplication1_GLJniLib_create
        (JNIEnv *env, jobject jobj) {




}


/*
 * Class:     com_example_tenny_myapplication1_OpenGLJniLib
 * Method:    step
 * Signature: ()V
 */
extern "C"
JNIEXPORT void JNICALL Java_com_example_tenny_myapplication1_GLJniLib_step
        (JNIEnv * env, jobject) {
    drawFrame();
}







