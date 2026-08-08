cmake_minimum_required(VERSION 3.16)

project(glm_wrapper C CXX)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Builds the JNI shared library by default. Native (cinterop) targets build
# only the static library by passing -DBUILD_JNI=OFF.
option(BUILD_JNI "Build the JNI shared library" ON)

# ---- GLM submodule + C API ----
set(GLM_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../glm)
set(C_API_DIR ${CMAKE_CURRENT_SOURCE_DIR}/c_api)

add_library(glm_wrapper STATIC
    ${C_API_DIR}/glm_wrapper.cpp
)

if(MSVC)
    target_compile_options(glm_wrapper PRIVATE /W3 /EHsc)
else()
    target_compile_options(glm_wrapper PRIVATE -Wall -fPIC -Wno-deprecated -fexceptions)
endif()

target_include_directories(glm_wrapper PUBLIC
    ${C_API_DIR}
    ${GLM_DIR}
)

set_target_properties(glm_wrapper PROPERTIES LINKER_LANGUAGE CXX)

# ---- JNI shared library ----
if(BUILD_JNI)
    add_library(glm_wrapper_jni SHARED ${CMAKE_CURRENT_SOURCE_DIR}/jni_bridge.cpp)
    set_target_properties(glm_wrapper_jni PROPERTIES OUTPUT_NAME glm_wrapper)
    if(WIN32)
        # MinGW prefixes DLLs with "lib"; strip it so the artifact is
        # glm_wrapper.dll everywhere (NativeLoader expects no prefix).
        set_target_properties(glm_wrapper_jni PROPERTIES PREFIX "")
    endif()

    if(ANDROID)
        find_library(log-lib log)
        if(log-lib)
            target_link_libraries(glm_wrapper_jni PRIVATE ${log-lib})
        endif()
    elseif(JNI_INCLUDE_DIR)
        target_include_directories(glm_wrapper_jni PRIVATE ${JNI_INCLUDE_DIR})
        if(JNI_INCLUDE_DIR_PLATFORM)
            target_include_directories(glm_wrapper_jni PRIVATE ${JNI_INCLUDE_DIR_PLATFORM})
        endif()
    else()
        find_package(JNI REQUIRED)
        target_include_directories(glm_wrapper_jni PRIVATE ${JNI_INCLUDE_DIRS})
    endif()

    target_link_libraries(glm_wrapper_jni PRIVATE glm_wrapper)
endif()
