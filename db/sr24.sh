#!/bin/bash

# version number - used in output file name
VER=1

# input file (./sr24.zip)
SR24FILE=${1:-.}
SR24FILE=${SR24FILE%/sr24.zip}/sr24.zip

# output file (./sr24.v1.db)
DBFILE=${2:-./sr24.v${VER}.db}

#---------------------------- tables --------------------------------
declare -a TABLES=(
    FD_GROUP 
    NUTR_DEF 
    WEIGHT
    NUT_DATA
    FOOTNOTE
    FOOD_DES
)

declare -a FD_GROUP=(fdgrp_cd fdgrp_desc)

declare -a NUTR_DEF=(
    nutr_no
    units
    tagname
    nutrdesc
    num_dec
    sr_order
)

declare -a WEIGHT=(
    ndb_no
    seq
    amount
    msre_desc
    gm_wgt
    num_data_pts
    std_dev
)

declare -a NUT_DATA=(
    ndb_no
    nutr_no
    nutr_val
    num_data_pts
    std_error
    src_cd
    deriv_cd
    ref_ndb_no
    add_nutr_mark
    num_studies
    min
    max
    df
    low_eb
    up_eb
    stat_cmt
    addmod_date
    cc
)

declare -a FOOTNOTE=(
    ndb_no
    footnt_no
    footnt_type
    nutr_no
    footnt_txt
)


declare -a FOOD_DES=(
    ndb_no
    fdgrp_cd
    long_desc
    shrt_desc
    comname
    manufacname
    survey
    ref_desc
    refuse
    sciname
    n_factor
    pro_factor
    fat_factor
    cho_factor
)
    

#---------------------------- functions -----------------------------

error() { echo "Error: ${1:-Unknown error}" >&2; exit 2; }

usage() {
    echo
    echo "Creates an SQLite DB from SR24 data files."
    echo "Usage:"
    echo "${0##*/} [sr24.zip] [outfile]"
    echo "where"
    echo "  sr24.zip = path to the sr24.zip file (default: ./)"
    echo "  outfile = name of output file (default: ./sr24.db)"
    echo
}

# create the DB tables ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#
createdb() {
cat << EOF
create table FD_GROUP (
    fdgrp_cd varchar(4) not null constraint pk_FD_GROUP primary key asc,
    fdgrp_desc varchar(60) not null
);

create table NUTR_DEF (
    nutr_no varchar(3) not null constraint pk_NUTR_DEF primary key asc,
    units varchar(7) not null,
    tagname varchar(20) default '',
    nutrdesc varchar(60) not null,
    num_dec varchar(1) not null,
    sr_order integer not null
);


create table WEIGHT (
    ndb_no varchar(5) not null references FOOD_DES(ndb_no)
        on delete restrict,
    seq varchar(2) not null,
    amount float not null,
    msre_desc varchar(80) not null,
    gm_wgt float not null,
    num_data_pts integer default null,
    std_dev float default null,
    primary key(ndb_no,seq)
);


create table NUT_DATA (
    ndb_no varchar(5) not null references FOOD_DES(ndb_no)
        on delete restrict,
    nutr_no varchar(3) not null references NUTR_DEF(nutr_no)
        on delete restrict,
    nutr_val float not null,
    num_data_pts integer not null,
    std_error float default null,
    src_cd varchar(2) not null,
    deriv_cd varchar(4) default null,
    ref_ndb_no varchar(5) default null,
    add_nutr_mark varchar(1) default null,
    num_studies integer default null,
    min float default null,
    max float default null,
    df integer default null,
    low_eb float default null,
    up_eb float default null,
    stat_cmt varchar(10) default null,
    addmod_date varchar(10) default null,
    cc varchar(1) default null,
    primary key (ndb_no,nutr_no)
);


create table FOOTNOTE (
    ndb_no varchar(5) not null references FOOD_DES(ndb_no)
        on delete restrict,
    footnt_no varchar(4) not null,
    footnt_type varchar(1) not null,
    nutr_no varchar(3) default null,
    footnt_txt varchar(200) not null
);

create table FOOD_DES (
    ndb_no varchar(5) not null constraint pk_FOOD_DES primary key asc,
    fdgrp_cd varchar(4) not null references FD_GROUP (fdgrp_cd)
        on delete restrict,
    long_desc varchar(200) not null collate nocase,
    shrt_desc varchar(60) not null collate nocase,
    comname varchar(100) default null collate nocase,
    manufacname varchar(65) default null collate nocase,
    survey varchar(1) default null,
    ref_desc varchar(135) default null,
    refuse integer default null,
    sciname varchar(65) default null,
    n_factor float default null,
    pro_factor float default null,
    fat_factor float default null,
    cho_factor float default null
);

create table DAILY_NUT (
    date text default CURRENT_DATE,
    time text default CURRENT_TIME,
    ndb_no varchar(5) not null references FOOD_DES(ndb_no)
        on delete restrict,
    seq varchar(2) not null,
    amount float not null,
    weight float not null
);
create index DAILY_NUT_IDATE on DAILY_NUT(date);
EOF
}
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# creates SQL insert statements for the given table
insert() {
    local name i j n c v
    name=${1}

    IFS=','
    eval "col=( \${$name[*]} )"

    while IFS='^' read -a val
    do
        unset c
        unset v
        let j=0
        n=${#val[*]}
        for (( i = 0 ; i < n ; i++ ))
        do
            if [[ -n "${val[$i]}" ]]
            then
                c[$j]=${col[$i]}
                v[$j]=${val[$i]}
                let j+=1
            fi
        done
        echo "insert into ${name}(${c[*]}) values (${v[*]});"
    done
}

# converts the DB input file format and create insert statements
tables() {
    local table
    for table in "${!TABLES[@]}"
    do
        name=${TABLES[${table}]}
        unzip -qqcaa ${SR24FILE} ${name}.txt |sed s/\'/\'\'/g | sed s/~/\'/g \
            | insert ${name}
    done
}

# Set initial display ordering
#
# 208|Energy|kcal
# 268|Energy|kJ
# 203|Protein|g
# 204|Total lipid (fat)|g
# 605|Fatty acids, total trans
# 606|Fatty acids, total saturated
# 645|Fatty acids, total monounsaturated
# 646|Fatty acids, total polyunsaturated
# 693|Fatty acids, total trans-monoenoic
# 695|Fatty acids, total trans-polyenoic
# 205|Carbohydrate, by difference|g
# 269|Sugars, total|g
# 291|Fiber, total dietary|g
# 307|Sodium, Na
#
dorder() {

    local i n
    let i=1
    for n in 268 203 204 605 606 205 269 291 307
    do
        echo "update NUTR_DEF set sr_order=${i} where nutr_no = ${n};";
        let i+=1
    done
}

# Add entries to the weight table for 100 gram units
addweight() {

    echo "insert into WEIGHT (ndb_no,seq,amount,msre_desc,gm_wgt)
        select distinct ndb_no,0,100.0,'gram',100.0 from WEIGHT 
        where seq=1;"
}


#---------------------------- main ----------------------------------
case ${1} in
    -* ) usage; exit 1 ;;
esac

[[ -f ${SR24FILE} ]] || error "File ${SR24FILE} not found"

rm -f ${DBFILE}
createdb | sqlite3 ${DBFILE}
tables | sqlite3 ${DBFILE}
dorder | sqlite3 ${DBFILE}
addweight | sqlite3 ${DBFILE}
