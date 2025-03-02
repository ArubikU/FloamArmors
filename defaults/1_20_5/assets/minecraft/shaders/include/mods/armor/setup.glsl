#ifdef VSH

if(getChannel(RelativeCords,ivec2(63,30), 0) == 0
&& getChannel(RelativeCords,ivec2(63,30), 1) == 0
&& getChannel(RelativeCords,ivec2(63,30), 2) == 0
&& getChannel(RelativeCords,ivec2(63,30), 3) == 255) {
    if(cube!=ARMOR_CHESTPLATE) {

        if((cube==ARMOR_RIGHT_ARM||cube==ARMOR_LEFT_ARM)
        ){
            if(uv.x<=32){

                if(cube==ARMOR_LEFT_ARM) {
                    cube = STASIS_LEFT_BOOT;
                }
                else if(cube==ARMOR_RIGHT_ARM) {
                    cube = STASIS_RIGHT_BOOT;
                }
            }else{
                
                if(cube==ARMOR_RIGHT_ARM) {
                    cube = STASIS_RIGHT_ARM;
                }
                else if(cube==ARMOR_LEFT_ARM) {
                    cube = STASIS_LEFT_ARM;
                }

            }
        }
        
        if(cube==ARMOR_LEFT_FEET) {
            cube = STASIS_RIGHT_BOOT;
        }
        else if(cube==ARMOR_RIGHT_FEET) {
            cube = STASIS_LEFT_BOOT;
        }
    }
    else{
            if(uv.x<=16){
        cube = STASIS_LEFT_BOOT;
            }else{
        cube = STASIS_CHESTPLATE;
            }
    }
    
}


if(getChannel(RelativeCords,ivec2(63,30), 0) == 255
&& getChannel(RelativeCords,ivec2(63,30), 1) == 255
&& getChannel(RelativeCords,ivec2(63,30), 2) == 255
&& getChannel(RelativeCords,ivec2(63,30), 3) == 255) {
    if(cube==ARMOR_RIGHT_ARM) {
        cube = STASIS_LEFT_LEG;
    }
    else if(cube==ARMOR_LEFT_ARM) {
        cube = STASIS_RIGHT_LEG;
    } else 
    if(cube==ARMOR_CHESTPLATE) {
        cube = STASIS_INNER_ARMOR;
    }
    
}
#endif