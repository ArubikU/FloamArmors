#define REBUILD_CHESTPLATE() {\

                        //ADD_SQUARE_WITH_ROTATION(vec3(-4, 2, 5), vec3(-9,2 ,5), vec3(-4, 2, 2), vec4(3,5,5,3),4);
                        ADD_BOX_WITH_ROTATION_ROTATE(vec3(0,0,0), vec3(sizes.x,sizes.y,0)*5.33,   //Pos, Size,
                         Rotate3(0,X), vec3(0, 0, 0),
                        vec4(stp.x,stp.y,res.x,res.y),vec4(stp.x,stp.y,res.x,res.y),vec4(stp.x,stp.y,res.x,res.y),
                        vec4(stp.x,stp.y,res.x,res.y),vec4(stp.x,stp.y,res.x,res.y),vec4(stp.x,stp.y,res.x,res.y),
                        2, 2,
                        2, 2,
                        2, 2);\
                        
                        
            }